using BembosEvents.Web.Models.MercadoPago;

namespace BembosEvents.Web.ViewModels;

public sealed class MercadoPagoBackofficeViewModel
{
    public IReadOnlyList<MercadoPagoPaymentDto> Pagos { get; init; } =
        Array.Empty<MercadoPagoPaymentDto>();

    public bool BackendAvailable { get; init; } =
        true;
}