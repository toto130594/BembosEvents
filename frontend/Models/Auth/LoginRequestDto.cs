namespace BembosEvents.Web.Models.Auth;

public sealed class LoginRequestDto
{
    public string Correo { get; init; } =
        string.Empty;

    public string Password { get; init; } =
        string.Empty;
}
