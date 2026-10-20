namespace BembosEvents.Web.Models.Auth;

public sealed class EmailVerificationTokenRequestDto
{
    public string Token { get; init; } =
        string.Empty;
}