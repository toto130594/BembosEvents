using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficeReservaDetalleViewModel
{
    public ReservaBackofficeDetalleDto Reserva { get; set; } =
        new();

    public IReadOnlyList<string> TransicionesDisponibles { get; set; } =
        Array.Empty<string>();

    public string? ErrorCambioEstado { get; set; }

    public string? MensajeExito { get; set; }
}
