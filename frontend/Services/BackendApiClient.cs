using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Models.Auth;

namespace BembosEvents.Web.Services;

public sealed class BackendApiClient : IBackendApiClient
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<BackendApiClient> _logger;

    public BackendApiClient(
        HttpClient httpClient,
        ILogger<BackendApiClient> logger)
    {
        _httpClient = httpClient;
        _logger = logger;
    }

    public async Task<BackendApiProbeResult> CheckConnectionAsync(
        CancellationToken cancellationToken = default)
    {
        try
        {
            using var response =
                await _httpClient.GetAsync(
                    "api/sedes",
                    cancellationToken);

            if (response.IsSuccessStatusCode)
            {
                return new BackendApiProbeResult(
                    true,
                    response.StatusCode,
                    "Spring Boot disponible.");
            }

            _logger.LogWarning(
                "Backend API respondio con HTTP {StatusCode}.",
                (int)response.StatusCode);

            return new BackendApiProbeResult(
                false,
                response.StatusCode,
                $"Spring Boot respondio HTTP {(int)response.StatusCode}.");
        }
        catch (OperationCanceledException)
            when (!cancellationToken.IsCancellationRequested)
        {
            _logger.LogWarning(
                "Timeout al conectar con Spring Boot.");

            return new BackendApiProbeResult(
                false,
                null,
                "Timeout al conectar con Spring Boot.");
        }
        catch (HttpRequestException exception)
        {
            _logger.LogWarning(
                exception,
                "No se pudo conectar con Spring Boot.");

            return new BackendApiProbeResult(
                false,
                null,
                "No se pudo conectar con Spring Boot.");
        }
    }

    public async Task<IReadOnlyList<SedePublicaDto>> GetSedesAsync(
        CancellationToken cancellationToken = default)
    {
        var sedes =
            await _httpClient.GetFromJsonAsync<List<SedePublicaDto>>(
                "api/sedes",
                cancellationToken);

        return sedes ??
            new List<SedePublicaDto>();
    }

    public Task<int> GetPaquetesCountAsync(
        CancellationToken cancellationToken = default)
    {
        return GetCollectionCountAsync(
            "api/paquetes",
            cancellationToken);
    }

    public Task<int> GetTematicasCountAsync(
        CancellationToken cancellationToken = default)
    {
        return GetCollectionCountAsync(
            "api/tematicas",
            cancellationToken);
    }

    public Task<int> GetServiciosAdicionalesCountAsync(
        CancellationToken cancellationToken = default)
    {
        return GetCollectionCountAsync(
            "api/servicios-adicionales",
            cancellationToken);
    }

    public async Task<BackendLoginResult> LoginAsync(
        LoginRequestDto request,
        CancellationToken cancellationToken = default)
    {
        try
        {
            using var response =
                await _httpClient.PostAsJsonAsync(
                    "api/auth/login",
                    request,
                    cancellationToken);

            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                return new BackendLoginResult(
                    false,
                    null,
                    response.StatusCode,
                    "Correo o contrasena incorrectos.");
            }

            if (response.StatusCode == HttpStatusCode.BadRequest)
            {
                return new BackendLoginResult(
                    false,
                    null,
                    response.StatusCode,
                    "Revisa los datos ingresados.");
            }

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning(
                    "Login backend respondio HTTP {StatusCode}.",
                    (int)response.StatusCode);

                return new BackendLoginResult(
                    false,
                    null,
                    response.StatusCode,
                    "No fue posible iniciar sesion.");
            }

            var auth =
                await response.Content
                    .ReadFromJsonAsync<AuthResponseDto>(
                        cancellationToken:
                            cancellationToken);

            if (
                auth is null ||
                string.IsNullOrWhiteSpace(auth.Token) ||
                auth.ExpiresInMs <= 0
            )
            {
                _logger.LogWarning(
                    "Login backend devolvio una respuesta incompleta.");

                return new BackendLoginResult(
                    false,
                    null,
                    response.StatusCode,
                    "La respuesta de autenticacion no es valida.");
            }

            return new BackendLoginResult(
                true,
                auth,
                response.StatusCode,
                null);
        }
        catch (OperationCanceledException)
            when (!cancellationToken.IsCancellationRequested)
        {
            _logger.LogWarning(
                "Timeout durante login contra Spring Boot.");

            return new BackendLoginResult(
                false,
                null,
                null,
                "El servicio de autenticacion demoro demasiado en responder.");
        }
        catch (HttpRequestException exception)
        {
            _logger.LogWarning(
                exception,
                "No fue posible conectar con autenticacion Spring Boot.");

            return new BackendLoginResult(
                false,
                null,
                null,
                "El servicio de autenticacion no esta disponible.");
        }
        catch (JsonException exception)
        {
            _logger.LogWarning(
                exception,
                "Respuesta JSON invalida durante login.");

            return new BackendLoginResult(
                false,
                null,
                null,
                "La respuesta de autenticacion no es valida.");
        }
    }

    public async Task<BackendRegistrationResult> RegisterAsync(
        RegistroRequestDto request,
        CancellationToken cancellationToken = default)
    {
        try
        {
            using var response =
                await _httpClient.PostAsJsonAsync(
                    "api/auth/registro",
                    request,
                    cancellationToken);

            var body =
                await response.Content.ReadAsStringAsync(
                    cancellationToken);

            if (response.StatusCode == HttpStatusCode.Created)
            {
                try
                {
                    var success =
                        JsonSerializer.Deserialize<MensajeResponseDto>(
                            body,
                            new JsonSerializerOptions
                            {
                                PropertyNameCaseInsensitive = true
                            });

                    return new BackendRegistrationResult(
                        true,
                        response.StatusCode,
                        success?.Mensaje ??
                        "Usuario registrado correctamente");
                }
                catch (JsonException)
                {
                    return new BackendRegistrationResult(
                        true,
                        response.StatusCode,
                        "Usuario registrado correctamente");
                }
            }

            var backendMessage =
                ExtractBackendMessage(
                    body);

            _logger.LogWarning(
                "Registro backend respondio HTTP {StatusCode}.",
                (int)response.StatusCode);

            return new BackendRegistrationResult(
                false,
                response.StatusCode,
                backendMessage ??
                "No fue posible registrar la cuenta.");
        }
        catch (OperationCanceledException)
            when (!cancellationToken.IsCancellationRequested)
        {
            _logger.LogWarning(
                "Timeout durante registro contra Spring Boot.");

            return new BackendRegistrationResult(
                false,
                null,
                "El servicio de registro demoro demasiado en responder.");
        }
        catch (HttpRequestException exception)
        {
            _logger.LogWarning(
                exception,
                "No fue posible conectar con registro Spring Boot.");

            return new BackendRegistrationResult(
                false,
                null,
                "El servicio de registro no esta disponible.");
        }
    }

    private static string? ExtractBackendMessage(
        string body)
    {
        if (string.IsNullOrWhiteSpace(body))
        {
            return null;
        }

        try
        {
            using var document =
                JsonDocument.Parse(
                    body);

            var root =
                document.RootElement;

            foreach (var preferredName in new[]
            {
                "mensaje",
                "message"
            })
            {
                if (
                    root.ValueKind == JsonValueKind.Object &&
                    root.TryGetProperty(
                        preferredName,
                        out var property) &&
                    property.ValueKind == JsonValueKind.String
                )
                {
                    var value =
                        property.GetString();

                    if (!string.IsNullOrWhiteSpace(value))
                    {
                        return value;
                    }
                }
            }

            return FindFirstUsefulString(
                root);
        }
        catch (JsonException)
        {
            return null;
        }
    }

    private static string? FindFirstUsefulString(
        JsonElement element)
    {
        if (element.ValueKind == JsonValueKind.String)
        {
            var value =
                element.GetString();

            if (
                !string.IsNullOrWhiteSpace(value) &&
                !string.Equals(
                    value,
                    "Bad Request",
                    StringComparison.OrdinalIgnoreCase) &&
                !string.Equals(
                    value,
                    "Conflict",
                    StringComparison.OrdinalIgnoreCase)
            )
            {
                return value;
            }

            return null;
        }

        if (element.ValueKind == JsonValueKind.Object)
        {
            foreach (var property in element.EnumerateObject())
            {
                var result =
                    FindFirstUsefulString(
                        property.Value);

                if (!string.IsNullOrWhiteSpace(result))
                {
                    return result;
                }
            }
        }

        if (element.ValueKind == JsonValueKind.Array)
        {
            foreach (var item in element.EnumerateArray())
            {
                var result =
                    FindFirstUsefulString(
                        item);

                if (!string.IsNullOrWhiteSpace(result))
                {
                    return result;
                }
            }
        }

        return null;
    }
    private async Task<int> GetCollectionCountAsync(
        string relativePath,
        CancellationToken cancellationToken)
    {
        using var response =
            await _httpClient.GetAsync(
                relativePath,
                cancellationToken);

        response.EnsureSuccessStatusCode();

        await using var stream =
            await response.Content.ReadAsStreamAsync(
                cancellationToken);

        using var document =
            await JsonDocument.ParseAsync(
                stream,
                cancellationToken:
                    cancellationToken);

        if (
            document.RootElement.ValueKind !=
            JsonValueKind.Array
        )
        {
            throw new JsonException(
                $"La respuesta de {relativePath} no es un arreglo JSON.");
        }

        return document
            .RootElement
            .GetArrayLength();
    }
}
