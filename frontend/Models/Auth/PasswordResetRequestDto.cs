namespace BembosEvents.Web.Models.Auth;

public sealed class PasswordResetRequestDto
{
    public string Token { get; init; } =
        string.Empty;

    public string NuevaPassword { get; init; } =
        string.Empty;
}