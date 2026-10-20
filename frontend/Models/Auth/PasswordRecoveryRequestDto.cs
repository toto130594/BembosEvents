namespace BembosEvents.Web.Models.Auth;

public sealed class PasswordRecoveryRequestDto
{
    public string Correo { get; init; } =
        string.Empty;
}