using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Models.Auth;

namespace BembosEvents.Web.Services;

public interface IBackendApiClient
{
    Task<BackendApiProbeResult> CheckConnectionAsync(
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<SedePublicaDto>> GetSedesAsync(
        CancellationToken cancellationToken = default);

    Task<int> GetPaquetesCountAsync(
        CancellationToken cancellationToken = default);

    Task<int> GetTematicasCountAsync(
        CancellationToken cancellationToken = default);

    Task<int> GetServiciosAdicionalesCountAsync(
        CancellationToken cancellationToken = default);

    Task<BackendLoginResult> LoginAsync(
        LoginRequestDto request,
        CancellationToken cancellationToken = default);
    Task<BackendRegistrationResult> RegisterAsync(
        RegistroRequestDto request,
        CancellationToken cancellationToken = default);
    Task<SedePublicaDto?> GetSedeAsync(
        int idSede,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<SalonPublicoDto>> GetSalonesBySedeAsync(
        int idSede,
        CancellationToken cancellationToken = default);

    Task<SalonPublicoDto?> GetSalonAsync(
        int idSalon,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<HorarioSalonDto>> GetHorariosSalonAsync(
        int idSalon,
        CancellationToken cancellationToken = default);
    Task<IReadOnlyList<PaquetePublicoDto>> GetPaquetesAsync(
        CancellationToken cancellationToken = default);

    Task<PaquetePublicoDto?> GetPaqueteAsync(
        int idPaquete,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<TematicaPublicaDto>> GetTematicasAsync(
        CancellationToken cancellationToken = default);

    Task<TematicaPublicaDto?> GetTematicaAsync(
        int idTematica,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<ServicioAdicionalPublicoDto>> GetServiciosAdicionalesAsync(
        CancellationToken cancellationToken = default);

    Task<ServicioAdicionalPublicoDto?> GetServicioAdicionalAsync(
        int idServicioAdicional,
        CancellationToken cancellationToken = default);
}
