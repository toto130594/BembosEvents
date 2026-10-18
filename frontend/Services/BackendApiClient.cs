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

    public async Task<SedePublicaDto?> GetSedeAsync(
        int idSede,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/sedes/{idSede}",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<SedePublicaDto>(
            cancellationToken: cancellationToken);
    }

    public async Task<IReadOnlyList<SalonPublicoDto>> GetSalonesBySedeAsync(
        int idSede,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/sedes/{idSede}/salones",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return Array.Empty<SalonPublicoDto>();
        }

        response.EnsureSuccessStatusCode();

        var salones =
            await response.Content.ReadFromJsonAsync<List<SalonPublicoDto>>(
                cancellationToken: cancellationToken);

        return salones ??
            new List<SalonPublicoDto>();
    }

    public async Task<SalonPublicoDto?> GetSalonAsync(
        int idSalon,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/salones/{idSalon}",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<SalonPublicoDto>(
            cancellationToken: cancellationToken);
    }

    public async Task<IReadOnlyList<HorarioSalonDto>> GetHorariosSalonAsync(
        int idSalon,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/salones/{idSalon}/horarios",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return Array.Empty<HorarioSalonDto>();
        }

        response.EnsureSuccessStatusCode();

        var horarios =
            await response.Content.ReadFromJsonAsync<List<HorarioSalonDto>>(
                cancellationToken: cancellationToken);

        return horarios ??
            new List<HorarioSalonDto>();
    }
    public async Task<IReadOnlyList<PaquetePublicoDto>> GetPaquetesAsync(
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/paquetes",
                cancellationToken);

        response.EnsureSuccessStatusCode();

        var items =
            await response.Content.ReadFromJsonAsync<List<PaquetePublicoDto>>(
                cancellationToken: cancellationToken);

        return items ??
            new List<PaquetePublicoDto>();
    }

    public async Task<PaquetePublicoDto?> GetPaqueteAsync(
        int idPaquete,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/paquetes/{idPaquete}",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<PaquetePublicoDto>(
            cancellationToken: cancellationToken);
    }

    public async Task<IReadOnlyList<TematicaPublicaDto>> GetTematicasAsync(
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/tematicas",
                cancellationToken);

        response.EnsureSuccessStatusCode();

        var items =
            await response.Content.ReadFromJsonAsync<List<TematicaPublicaDto>>(
                cancellationToken: cancellationToken);

        return items ??
            new List<TematicaPublicaDto>();
    }

    public async Task<TematicaPublicaDto?> GetTematicaAsync(
        int idTematica,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/tematicas/{idTematica}",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<TematicaPublicaDto>(
            cancellationToken: cancellationToken);
    }

    public async Task<IReadOnlyList<ServicioAdicionalPublicoDto>> GetServiciosAdicionalesAsync(
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/servicios-adicionales",
                cancellationToken);

        response.EnsureSuccessStatusCode();

        var items =
            await response.Content.ReadFromJsonAsync<List<ServicioAdicionalPublicoDto>>(
                cancellationToken: cancellationToken);

        return items ??
            new List<ServicioAdicionalPublicoDto>();
    }

    public async Task<ServicioAdicionalPublicoDto?> GetServicioAdicionalAsync(
        int idServicioAdicional,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/servicios-adicionales/{idServicioAdicional}",
                cancellationToken);

        if (response.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<ServicioAdicionalPublicoDto>(
            cancellationToken: cancellationToken);
    }
    public async Task<DisponibilidadSalonDto?> GetDisponibilidadSalonAsync(
        int idSalon,
        DateOnly fecha,
        TimeOnly horaInicio,
        TimeOnly horaFin,
        CancellationToken cancellationToken = default)
    {
        var fechaValue =
            fecha.ToString(
                "yyyy-MM-dd",
                System.Globalization.CultureInfo.InvariantCulture);

        var horaInicioValue =
            horaInicio.ToString(
                "HH:mm",
                System.Globalization.CultureInfo.InvariantCulture);

        var horaFinValue =
            horaFin.ToString(
                "HH:mm",
                System.Globalization.CultureInfo.InvariantCulture);

        var uri =
            "api/salones/" +
            idSalon +
            "/disponibilidad?fecha=" +
            Uri.EscapeDataString(fechaValue) +
            "&horaInicio=" +
            Uri.EscapeDataString(horaInicioValue) +
            "&horaFin=" +
            Uri.EscapeDataString(horaFinValue);

        using var response =
            await _httpClient.GetAsync(
                uri,
                cancellationToken);

        if (
            response.StatusCode ==
            System.Net.HttpStatusCode.NotFound
        )
        {
            return null;
        }

        response.EnsureSuccessStatusCode();

        return await response.Content
            .ReadFromJsonAsync<DisponibilidadSalonDto>(
                cancellationToken: cancellationToken);
    }

    public async Task<BembosEvents.Web.Models.Api.BackendApiResult<BembosEvents.Web.Models.Api.ReservaCreadaDto>> CrearReservaAsync(
        BembosEvents.Web.Models.Api.CrearReservaRequestDto request,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsJsonAsync(
                "api/reservas",
                request,
                cancellationToken);

        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content.ReadFromJsonAsync<BembosEvents.Web.Models.Api.ReservaCreadaDto>(
                    cancellationToken: cancellationToken);

            return new BembosEvents.Web.Models.Api.BackendApiResult<BembosEvents.Web.Models.Api.ReservaCreadaDto>
            {
                StatusCode =
                    (int)response.StatusCode,

                Data =
                    data
            };
        }

        var errorBody =
            await response.Content.ReadAsStringAsync(
                cancellationToken);

        return new BembosEvents.Web.Models.Api.BackendApiResult<BembosEvents.Web.Models.Api.ReservaCreadaDto>
        {
            StatusCode =
                (int)response.StatusCode,

            ErrorBody =
                errorBody
        };
    }

    public async Task<BackendApiResult<IReadOnlyList<ReservaResumenDto>>> GetMisReservasAsync(
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/reservas/mias",
                cancellationToken);

        if (response.IsSuccessStatusCode)
        {
            var items =
                await response.Content.ReadFromJsonAsync<List<ReservaResumenDto>>(
                    cancellationToken: cancellationToken);

            return new BackendApiResult<IReadOnlyList<ReservaResumenDto>>
            {
                StatusCode =
                    (int)response.StatusCode,

                Data =
                    items ??
                    new List<ReservaResumenDto>()
            };
        }

        return new BackendApiResult<IReadOnlyList<ReservaResumenDto>>
        {
            StatusCode =
                (int)response.StatusCode,

            ErrorBody =
                await response.Content.ReadAsStringAsync(
                    cancellationToken)
        };
    }

    public async Task<BackendApiResult<ReservaDetalleDto>> GetReservaPropiaAsync(
        int idReserva,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/reservas/{idReserva}",
                cancellationToken);

        if (response.IsSuccessStatusCode)
        {
            var item =
                await response.Content.ReadFromJsonAsync<ReservaDetalleDto>(
                    cancellationToken: cancellationToken);

            return new BackendApiResult<ReservaDetalleDto>
            {
                StatusCode =
                    (int)response.StatusCode,

                Data =
                    item
            };
        }

        return new BackendApiResult<ReservaDetalleDto>
        {
            StatusCode =
                (int)response.StatusCode,

            ErrorBody =
                await response.Content.ReadAsStringAsync(
                    cancellationToken)
        };
    }

    public async Task<BackendApiResult<ReservaDetalleDto>> CancelarReservaPropiaAsync(
        int idReserva,
        CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                $"api/reservas/{idReserva}/cancelacion",
                content: null,
                cancellationToken);

        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content.ReadFromJsonAsync<ReservaDetalleDto>(
                    cancellationToken: cancellationToken);

            return new BackendApiResult<ReservaDetalleDto>
            {
                StatusCode =
                    (int)response.StatusCode,

                Data =
                    data
            };
        }

        return new BackendApiResult<ReservaDetalleDto>
        {
            StatusCode =
                (int)response.StatusCode,

            ErrorBody =
                await response.Content.ReadAsStringAsync(
                    cancellationToken)
        };
    }
}
