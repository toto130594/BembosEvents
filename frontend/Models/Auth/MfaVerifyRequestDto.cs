namespace BembosEvents.Web.Models.Auth;

public sealed class MfaVerifyRequestDto
{
    public string ChallengeToken { get; init; } =
        string.Empty;

    public string Code { get; init; } =
        string.Empty;
}