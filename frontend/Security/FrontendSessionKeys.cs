namespace BembosEvents.Web.Security;

public static class FrontendSessionKeys
{
    public const string BackendJwt =
        "BackendJwt";

    public const string BackendJwtExpiresAtUtc =
        "BackendJwtExpiresAtUtc";

    public const string BackendRefreshToken =
        "BackendRefreshToken";

    public const string BackendRefreshTokenExpiresAtUtc =
        "BackendRefreshTokenExpiresAtUtc";

    public const string PendingMfaChallengeToken =
        "PendingMfaChallengeToken";

    public const string PendingMfaChallengeExpiresAtUtc =
        "PendingMfaChallengeExpiresAtUtc";

    public const string PendingMfaEmail =
        "PendingMfaEmail";

    public const string PendingMfaReturnUrl =
        "PendingMfaReturnUrl";

    public const string ReservationDraft =
        "BembosEvents.ReservationDraft";
}
