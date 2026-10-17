namespace BembosEvents.Web.Models.Api;

public sealed class ServicioAdicionalPublicoDto
{
    public int IdServicioAdicional { get; init; }

    public string Nombre { get; init; } =
        string.Empty;

    public string? Descripcion { get; init; }

    public decimal Precio { get; init; }
}
