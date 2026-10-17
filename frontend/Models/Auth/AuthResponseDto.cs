namespace BembosEvents.Web.Models.Auth;

public sealed class AuthResponseDto
{
    public string Token { get; init; } =
        string.Empty;

    public string Tipo { get; init; } =
        string.Empty;

    public long ExpiresInMs { get; init; }
}
