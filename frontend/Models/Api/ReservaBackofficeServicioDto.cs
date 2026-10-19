namespace BembosEvents.Web.Models.Api;

public sealed class ReservaBackofficeServicioDto
{
    public int IdServicioAdicional { get; set; }

    public int Cantidad { get; set; }

    public decimal PrecioUnitario { get; set; }

    public decimal Subtotal { get; set; }
}
