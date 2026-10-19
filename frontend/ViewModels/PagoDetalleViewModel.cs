using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class PagoDetalleViewModel
{
    public PagoDto Pago { get; init; } =
        new();
}
