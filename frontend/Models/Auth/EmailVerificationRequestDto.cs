namespace BembosEvents.Web.Models.Auth;

public sealed class EmailVerificationRequestDto
{
    public string Correo { get; init; } =
        string.Empty;
}