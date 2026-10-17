namespace BembosEvents.Web.Models.Api;

public sealed class PaquetePublicoDto
{
    public int IdPaquete { get; init; }

    public string Nombre { get; init; } =
        string.Empty;

    public string? Descripcion { get; init; }

    public decimal PrecioBase { get; init; }

    public int CantidadPersonasBase { get; init; }
}
