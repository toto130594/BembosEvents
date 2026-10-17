namespace BembosEvents.Web.Models.Api;

public sealed class TematicaPublicaDto
{
    public int IdTematica { get; init; }

    public string Nombre { get; init; } =
        string.Empty;

    public string? Descripcion { get; init; }

    public decimal PrecioAdicional { get; init; }
}
