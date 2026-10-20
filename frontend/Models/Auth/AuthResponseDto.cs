namespace BembosEvents.Web.Models.Auth;

public sealed class AuthResponseDto
{
    public string? Token { get; init; }

    public string? Tipo { get; init; }

    public long ExpiresInMs { get; init; }

    public string? RefreshToken { get; init; }

    public long RefreshTokenExpiresInMs { get; init; }

    public bool MfaRequired { get; init; }

    public string? MfaChallengeToken { get; init; }

    public long MfaChallengeExpiresInMs { get; init; }
}