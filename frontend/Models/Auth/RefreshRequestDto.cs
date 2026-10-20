namespace BembosEvents.Web.Models.Auth;

public sealed class RefreshRequestDto
{
    public string RefreshToken { get; init; } =
        string.Empty;
}