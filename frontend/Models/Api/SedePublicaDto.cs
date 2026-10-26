namespace BembosEvents.Web.Models.Api;

public sealed class SedePublicaDto
{
    public int IdSede { get; init; }

    public string Nombre { get; init; } = string.Empty;

    public string Direccion { get; init; } = string.Empty;

    public string Distrito { get; init; } = string.Empty;

    public string Telefono { get; init; } = string.Empty;

    public string TipoLocal { get; init; } =
        "RESTAURANTE";

    public bool PermiteEventos { get; init; } =
        true;
}
