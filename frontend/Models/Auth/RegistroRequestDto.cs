namespace BembosEvents.Web.Models.Auth;

public sealed class RegistroRequestDto
{
    public string Nombres { get; init; } =
        string.Empty;

    public string Apellidos { get; init; } =
        string.Empty;

    public string Correo { get; init; } =
        string.Empty;

    public string? Telefono { get; init; }

    public string Password { get; init; } =
        string.Empty;
}
