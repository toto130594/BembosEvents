namespace BembosEvents.Web.Models.Auth;

public sealed class LogoutRequestDto
{
    public string RefreshToken { get; init; } =
        string.Empty;
}