namespace BembosEvents.Web.Models.Auth;

public sealed class BackendSessionDto
{
    public long IdSesionUsuario { get; init; }

    public DateTime FechaCreacion { get; init; }

    public DateTime FechaExpiracion { get; init; }

    public DateTime FechaUltimoUso { get; init; }

    public string? UserAgent { get; init; }

    public string? IpOrigen { get; init; }
}