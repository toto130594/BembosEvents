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
}
