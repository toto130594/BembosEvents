using System.Collections.Concurrent;
using System.Globalization;
using System.Net;
using System.Net.Http.Json;
using BembosEvents.Web.Models.Auth;
using BembosEvents.Web.Security;

namespace BembosEvents.Web.Services;

public sealed class BackendTokenRefreshService
{
    private static readonly ConcurrentDictionary<
        string,
        RefreshLockEntry> RefreshLocks =
            new();

    private sealed class RefreshLockEntry
    {
        public object SyncRoot { get; } =
            new();

        public SemaphoreSlim Semaphore { get; } =
            new(1, 1);

        public int ReferenceCount { get; set; }
    }

    private static readonly TimeSpan RefreshWindow =
        TimeSpan.FromSeconds(30);

    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IHttpContextAccessor _httpContextAccessor;
    private readonly ILogger<BackendTokenRefreshService> _logger;

    public BackendTokenRefreshService(
        IHttpClientFactory httpClientFactory,
        IHttpContextAccessor httpContextAccessor,
        ILogger<BackendTokenRefreshService> logger)
    {
        _httpClientFactory = httpClientFactory;
        _httpContextAccessor = httpContextAccessor;
        _logger = logger;
    }

    public async Task<string?> GetValidAccessTokenAsync(
        CancellationToken cancellationToken = default)
    {
        var httpContext =
            _httpContextAccessor.HttpContext;

        if (httpContext is null)
        {
            return null;
        }

        var session =
            httpContext.Session;

        var currentToken =
            session.GetString(
                FrontendSessionKeys.BackendJwt);

        var currentExpiration =
            GetExpiration(
                session,
                FrontendSessionKeys.BackendJwtExpiresAtUtc);

        var now =
            DateTimeOffset.UtcNow;

        if (
            !string.IsNullOrWhiteSpace(currentToken) &&
            currentExpiration.HasValue &&
            currentExpiration.Value >
                now.Add(RefreshWindow)
        )
        {
            return currentToken;
        }

        var refreshLock =
            AcquireRefreshLock(
                session.Id);

        var lockAcquired =
            false;

        try
        {
            await refreshLock.Semaphore.WaitAsync(
                cancellationToken);

            lockAcquired =
                true;
            currentToken =
                session.GetString(
                    FrontendSessionKeys.BackendJwt);

            currentExpiration =
                GetExpiration(
                    session,
                    FrontendSessionKeys.BackendJwtExpiresAtUtc);

            now =
                DateTimeOffset.UtcNow;

            if (
                !string.IsNullOrWhiteSpace(currentToken) &&
                currentExpiration.HasValue &&
                currentExpiration.Value >
                    now.Add(RefreshWindow)
            )
            {
                return currentToken;
            }

            var refreshToken =
                session.GetString(
                    FrontendSessionKeys.BackendRefreshToken);

            var refreshExpiration =
                GetExpiration(
                    session,
                    FrontendSessionKeys.BackendRefreshTokenExpiresAtUtc);

            if (
                string.IsNullOrWhiteSpace(refreshToken) ||
                !refreshExpiration.HasValue ||
                refreshExpiration.Value <= now
            )
            {
                ClearBackendAuthentication(
                    session);

                return null;
            }

            var client =
                _httpClientFactory.CreateClient(
                    "BackendAuthRefresh");

            using var response =
                await client.PostAsJsonAsync(
                    "api/auth/refresh",
                    new RefreshRequestDto
                    {
                        RefreshToken =
                            refreshToken
                    },
                    cancellationToken);

            if (
                response.StatusCode ==
                    HttpStatusCode.Unauthorized ||
                response.StatusCode ==
                    HttpStatusCode.BadRequest
            )
            {
                ClearBackendAuthentication(
                    session);

                _logger.LogInformation(
                    "Refresh backend rechazado; se elimino la sesion backend local.");

                return null;
            }

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning(
                    "Refresh backend respondio HTTP {StatusCode}.",
                    (int)response.StatusCode);

                return null;
            }

            var auth =
                await response.Content
                    .ReadFromJsonAsync<AuthResponseDto>(
                        cancellationToken:
                            cancellationToken);

            if (
                auth is null ||
                auth.MfaRequired ||
                string.IsNullOrWhiteSpace(auth.Token) ||
                auth.ExpiresInMs <= 0 ||
                string.IsNullOrWhiteSpace(auth.RefreshToken) ||
                auth.RefreshTokenExpiresInMs <= 0
            )
            {
                _logger.LogWarning(
                    "Refresh backend devolvio una respuesta invalida.");

                return null;
            }

            var accessExpiresAt =
                DateTimeOffset.UtcNow
                    .AddMilliseconds(
                        auth.ExpiresInMs);

            var refreshExpiresAt =
                DateTimeOffset.UtcNow
                    .AddMilliseconds(
                        auth.RefreshTokenExpiresInMs);

            session.SetString(
                FrontendSessionKeys.BackendJwt,
                auth.Token);

            session.SetString(
                FrontendSessionKeys.BackendJwtExpiresAtUtc,
                accessExpiresAt.ToString(
                    "O",
                    CultureInfo.InvariantCulture));

            session.SetString(
                FrontendSessionKeys.BackendRefreshToken,
                auth.RefreshToken);

            session.SetString(
                FrontendSessionKeys.BackendRefreshTokenExpiresAtUtc,
                refreshExpiresAt.ToString(
                    "O",
                    CultureInfo.InvariantCulture));

            _logger.LogDebug(
                "Access token backend renovado correctamente.");

            return auth.Token;
        }
        catch (OperationCanceledException)
            when (!cancellationToken.IsCancellationRequested)
        {
            _logger.LogWarning(
                "Timeout durante refresh backend.");

            return null;
        }
        catch (HttpRequestException exception)
        {
            _logger.LogWarning(
                exception,
                "No fue posible conectar con el endpoint de refresh backend.");

            return null;
        }
        catch (System.Text.Json.JsonException exception)
        {
            _logger.LogWarning(
                exception,
                "Respuesta JSON invalida durante refresh backend.");

            return null;
        }
        finally
        {
            if (lockAcquired)
            {
                refreshLock.Semaphore.Release();
            }

            ReleaseRefreshLock(
                session.Id,
                refreshLock);
        }
    }

    private static RefreshLockEntry AcquireRefreshLock(
        string sessionId)
    {
        while (true)
        {
            var entry =
                RefreshLocks.GetOrAdd(
                    sessionId,
                    _ => new RefreshLockEntry());

            lock (entry.SyncRoot)
            {
                if (
                    RefreshLocks.TryGetValue(
                        sessionId,
                        out var current) &&
                    ReferenceEquals(
                        current,
                        entry)
                )
                {
                    entry.ReferenceCount++;

                    return entry;
                }
            }
        }
    }

    private static void ReleaseRefreshLock(
        string sessionId,
        RefreshLockEntry entry)
    {
        lock (entry.SyncRoot)
        {
            entry.ReferenceCount--;

            if (entry.ReferenceCount < 0)
            {
                throw new InvalidOperationException(
                    "Refresh lock reference count cannot be negative.");
            }

            if (entry.ReferenceCount != 0)
            {
                return;
            }

            if (
                RefreshLocks.TryGetValue(
                    sessionId,
                    out var current) &&
                ReferenceEquals(
                    current,
                    entry)
            )
            {
                RefreshLocks.TryRemove(
                    sessionId,
                    out _);
            }
        }
    }
    private static DateTimeOffset? GetExpiration(
        ISession session,
        string key)
    {
        var value =
            session.GetString(
                key);

        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        if (
            DateTimeOffset.TryParse(
                value,
                CultureInfo.InvariantCulture,
                DateTimeStyles.RoundtripKind,
                out var expiration)
        )
        {
            return expiration;
        }

        return null;
    }

    private static void ClearBackendAuthentication(
        ISession session)
    {
        session.Remove(
            FrontendSessionKeys.BackendJwt);

        session.Remove(
            FrontendSessionKeys.BackendJwtExpiresAtUtc);

        session.Remove(
            FrontendSessionKeys.BackendRefreshToken);

        session.Remove(
            FrontendSessionKeys.BackendRefreshTokenExpiresAtUtc);
    }
}