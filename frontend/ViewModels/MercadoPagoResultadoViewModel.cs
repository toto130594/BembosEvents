using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class MercadoPagoResultadoViewModel
{
    public string ResultadoRetorno { get; init; } =
        string.Empty;

    public PagoDto? Pago { get; init; }

    public ReservaDetalleDto? Reserva { get; init; }

    public IReadOnlyList<PagoDto> Pagos { get; init; } =
        Array.Empty<PagoDto>();

    public decimal TotalPagado { get; init; }

    public decimal SaldoPendiente { get; init; }

    public string? Mensaje { get; init; }

    public int Intento { get; init; }

    public bool PagoAprobado =>
        string.Equals(
            Pago?.EstadoPago,
            "APROBADO",
            StringComparison.OrdinalIgnoreCase);

    public bool PagoPendiente =>
        string.Equals(
            Pago?.EstadoPago,
            "PENDIENTE",
            StringComparison.OrdinalIgnoreCase);

    public bool RetornoExitoso =>
        string.Equals(
            ResultadoRetorno,
            "success",
            StringComparison.OrdinalIgnoreCase);
}