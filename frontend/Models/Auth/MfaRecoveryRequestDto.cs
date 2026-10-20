namespace BembosEvents.Web.Models.Auth;

public sealed class MfaRecoveryRequestDto
{
    public string ChallengeToken { get; init; } =
        string.Empty;

    public string RecoveryCode { get; init; } =
        string.Empty;
}