using System.Net.Http.Json;
using System.Text.Json;
using BembosEvents.Web.Models.MercadoPago;

namespace BembosEvents.Web.Services;

public sealed class MercadoPagoApiClient :
    IMercadoPagoApiClient
{
    private static readonly JsonSerializerOptions JsonOptions =
        new()
        {
            PropertyNameCaseInsensitive = true
        };

    private readonly HttpClient _httpClient;

    public MercadoPagoApiClient(
        HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<MercadoPagoApiResult<MercadoPagoCheckoutDto>>
        StartCheckoutAsync(
            int idReserva,
            decimal monto,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsJsonAsync(
                $"api/reservas/{idReserva}/pagos/checkout",
                new MercadoPagoCheckoutRequestDto
                {
                    Monto = monto
                },
                cancellationToken);

        return await ReadAsync<MercadoPagoCheckoutDto>(
            response,
            cancellationToken);
    }

    public async Task<MercadoPagoApiResult<IReadOnlyList<MercadoPagoPaymentDto>>>
        GetBackofficePaymentsAsync(
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/backoffice/pagos",
                cancellationToken);

        if (!response.IsSuccessStatusCode)
        {
            return new MercadoPagoApiResult<IReadOnlyList<MercadoPagoPaymentDto>>
            {
                StatusCode =
                    (int)response.StatusCode,

                ErrorBody =
                    await response.Content.ReadAsStringAsync(
                        cancellationToken)
            };
        }

        var items =
            await response.Content.ReadFromJsonAsync<
                List<MercadoPagoPaymentDto>>(
                JsonOptions,
                cancellationToken);

        return new MercadoPagoApiResult<IReadOnlyList<MercadoPagoPaymentDto>>
        {
            StatusCode =
                (int)response.StatusCode,

            Data =
                items ??
                new List<MercadoPagoPaymentDto>()
        };
    }

    public async Task<MercadoPagoApiResult<MercadoPagoRefundDto>>
        RefundAsync(
            int idPago,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                $"api/backoffice/pagos/{idPago}/reembolso",
                null,
                cancellationToken);

        return await ReadAsync<MercadoPagoRefundDto>(
            response,
            cancellationToken);
    }

    public async Task<MercadoPagoApiResult<MercadoPagoReconciliationItemDto>>
        ReconcileAsync(
            int idPago,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                $"api/backoffice/pagos/mercado-pago/{idPago}/reconciliar",
                null,
                cancellationToken);

        return await ReadAsync<MercadoPagoReconciliationItemDto>(
            response,
            cancellationToken);
    }

    public async Task<MercadoPagoApiResult<MercadoPagoReconciliationDto>>
        ReconcileBatchAsync(
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                "api/backoffice/pagos/mercado-pago/reconciliar",
                null,
                cancellationToken);

        return await ReadAsync<MercadoPagoReconciliationDto>(
            response,
            cancellationToken);
    }

    private static async Task<MercadoPagoApiResult<T>>
        ReadAsync<T>(
            HttpResponseMessage response,
            CancellationToken cancellationToken)
    {
        if (!response.IsSuccessStatusCode)
        {
            return new MercadoPagoApiResult<T>
            {
                StatusCode =
                    (int)response.StatusCode,

                ErrorBody =
                    await response.Content.ReadAsStringAsync(
                        cancellationToken)
            };
        }

        var data =
            await response.Content.ReadFromJsonAsync<T>(
                JsonOptions,
                cancellationToken);

        return new MercadoPagoApiResult<T>
        {
            StatusCode =
                (int)response.StatusCode,

            Data =
                data
        };
    }
}