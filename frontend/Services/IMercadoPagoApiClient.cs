using BembosEvents.Web.Models.MercadoPago;

namespace BembosEvents.Web.Services;

public interface IMercadoPagoApiClient
{
    Task<MercadoPagoApiResult<MercadoPagoCheckoutDto>>
        StartCheckoutAsync(
            int idReserva,
            decimal monto,
            CancellationToken cancellationToken = default);

    Task<MercadoPagoApiResult<IReadOnlyList<MercadoPagoPaymentDto>>>
        GetBackofficePaymentsAsync(
            CancellationToken cancellationToken = default);

    Task<MercadoPagoApiResult<MercadoPagoRefundDto>>
        RefundAsync(
            int idPago,
            CancellationToken cancellationToken = default);

    Task<MercadoPagoApiResult<MercadoPagoReconciliationItemDto>>
        ReconcileAsync(
            int idPago,
            CancellationToken cancellationToken = default);

    Task<MercadoPagoApiResult<MercadoPagoReconciliationDto>>
        ReconcileBatchAsync(
            CancellationToken cancellationToken = default);
}