namespace BembosEvents.Web.Services;

public sealed class BackofficeApiResult<T>
{
    public BackofficeApiResult(
        int statusCode,
        T? data)
    {
        StatusCode = statusCode;
        Data = data;
    }

    public int StatusCode { get; }

    public T? Data { get; }

    public bool Success =>
        StatusCode >= 200 &&
        StatusCode <= 299;
}
