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

    Task<DisponibilidadSalonDto?> GetDisponibilidadSalonAsync(
        int idSalon,
        DateOnly fecha,
        TimeOnly horaInicio,
        TimeOnly horaFin,
        CancellationToken cancellationToken = default);

    Task<BembosEvents.Web.Models.Api.BackendApiResult<BembosEvents.Web.Models.Api.ReservaCreadaDto>> CrearReservaAsync(
        BembosEvents.Web.Models.Api.CrearReservaRequestDto request,
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<IReadOnlyList<ReservaResumenDto>>> GetMisReservasAsync(
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<ReservaDetalleDto>> GetReservaPropiaAsync(
        int idReserva,
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<ReservaDetalleDto>> CancelarReservaPropiaAsync(
        int idReserva,
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<PagoDto>> CrearPagoAsync(
        int idReserva,
        CrearPagoRequestDto request,
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<IReadOnlyList<PagoDto>>> GetPagosReservaAsync(
        int idReserva,
        CancellationToken cancellationToken = default);

    Task<BackendApiResult<PagoDto>> GetPagoPropioAsync(
        int idPago,
        CancellationToken cancellationToken = default);

    Task<int> GetBackofficeAccessStatusAsync(
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<System.Collections.Generic.IReadOnlyList<BembosEvents.Web.Models.Api.SedeBackofficeDto>>> GetBackofficeSedesAsync(
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<BembosEvents.Web.Models.Api.SedeBackofficeDto>> GetBackofficeSedeAsync(
        int idSede,
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<System.Collections.Generic.IReadOnlyList<BembosEvents.Web.Models.Api.ReservaBackofficeResumenDto>>> GetBackofficeReservasAsync(
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<BembosEvents.Web.Models.Api.ReservaBackofficeDetalleDto>> GetBackofficeReservaAsync(
        int idReserva,
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<BembosEvents.Web.Models.Api.ReservaBackofficeDetalleDto>> CambiarEstadoReservaBackofficeAsync(
        int idReserva,
        BembosEvents.Web.Models.Api.CambiarEstadoReservaRequestDto request,
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<System.Collections.Generic.IReadOnlyList<BembosEvents.Web.Models.Api.PagoDto>>> GetBackofficePagosAsync(
        CancellationToken cancellationToken = default);

    Task<BackofficeApiResult<BembosEvents.Web.Models.Api.PagoDto>> GetBackofficePagoAsync(
        int idPago,
        CancellationToken cancellationToken = default);
}
