using System.Net.Http.Headers;
using BembosEvents.Web.Services;

namespace BembosEvents.Web.Security;

public sealed class BackendBearerHandler : DelegatingHandler
{
    private readonly BackendTokenRefreshService _tokenRefreshService;

    public BackendBearerHandler(
        BackendTokenRefreshService tokenRefreshService)
    {
        _tokenRefreshService =
            tokenRefreshService;
    }

    protected override async Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request,
        CancellationToken cancellationToken)
    {
        var path =
            request.RequestUri?.AbsolutePath ??
            string.Empty;

        if (IsAnonymousAuthPath(path))
        {
            request.Headers.Authorization =
                null;

            return await base.SendAsync(
                request,
                cancellationToken);
        }

        var token =
            await _tokenRefreshService
                .GetValidAccessTokenAsync(
                    cancellationToken);

        if (!string.IsNullOrWhiteSpace(token))
        {
            request.Headers.Authorization =
                new AuthenticationHeaderValue(
                    "Bearer",
                    token);
        }
        else
        {
            request.Headers.Authorization =
                null;
        }

        return await base.SendAsync(
            request,
            cancellationToken);
    }

    private static bool IsAnonymousAuthPath(
        string path)
    {
        if (
            !path.StartsWith(
                "/api/auth/",
                StringComparison.OrdinalIgnoreCase)
        )
        {
            return false;
        }

        if (
            path.Equals(
                "/api/auth/logout-all",
                StringComparison.OrdinalIgnoreCase)
        )
        {
            return false;
        }

        if (
            path.Equals(
                "/api/auth/sessions",
                StringComparison.OrdinalIgnoreCase) ||
            path.StartsWith(
                "/api/auth/sessions/",
                StringComparison.OrdinalIgnoreCase)
        )
        {
            return false;
        }

        return true;
    }
}