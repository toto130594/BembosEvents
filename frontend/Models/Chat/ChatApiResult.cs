namespace BembosEvents.Web.Models.Chat;

public sealed class ChatApiResult<T>
{
    public int StatusCode { get; init; }

    public T? Data { get; init; }

    public string? ErrorBody { get; init; }

    public bool Success =>
        StatusCode >= 200 &&
        StatusCode < 300;
}