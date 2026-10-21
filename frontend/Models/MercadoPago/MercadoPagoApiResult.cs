namespace BembosEvents.Web.Models.MercadoPago;

public sealed class MercadoPagoApiResult<T>
{
    public int StatusCode { get; init; }

    public T? Data { get; init; }

    public string? ErrorBody { get; init; }

    public bool Success =>
        StatusCode >= 200 &&
        StatusCode < 300;
}