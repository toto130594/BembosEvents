namespace BembosEvents.Web.Models.Api;

public sealed class BackendApiResult<T>
{
    public int StatusCode { get; init; }

    public T? Data { get; init; }

    public string? ErrorBody { get; init; }

    public bool IsSuccess =>
        StatusCode >= 200 &&
        StatusCode <= 299;
}
