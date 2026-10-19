using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class PagosReservaViewModel
{
    public ReservaDetalleDto Reserva { get; init; } =
        new();

    public IReadOnlyList<PagoDto> Pagos { get; init; } =
        Array.Empty<PagoDto>();
}
