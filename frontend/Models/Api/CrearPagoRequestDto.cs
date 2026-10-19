namespace BembosEvents.Web.Models.Api;

public sealed class CrearPagoRequestDto
{
    public decimal Monto { get; set; }

    public string MetodoPago { get; set; } =
        string.Empty;
}
