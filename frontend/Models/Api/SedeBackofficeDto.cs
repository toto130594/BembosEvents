namespace BembosEvents.Web.Models.Api;

public sealed class SedeBackofficeDto
{
    public int IdSede { get; set; }

    public string Nombre { get; set; } =
        string.Empty;

    public string Direccion { get; set; } =
        string.Empty;

    public string Distrito { get; set; } =
        string.Empty;

    public string? Telefono { get; set; }

    public bool Activo { get; set; }
}
