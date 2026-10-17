namespace BembosEvents.Web.Models.Api;

public sealed class SalonPublicoDto
{
    public int IdSalon { get; init; }

    public int IdSede { get; init; }

    public string Nombre { get; init; } =
        string.Empty;

    public string? Descripcion { get; init; }

    public int Capacidad { get; init; }
}
