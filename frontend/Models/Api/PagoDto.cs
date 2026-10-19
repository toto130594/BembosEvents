namespace BembosEvents.Web.Models.Api;

public sealed class PagoDto
{
    public int IdPago { get; set; }

    public int IdReserva { get; set; }

    public decimal Monto { get; set; }

    public string MetodoPago { get; set; } =
        string.Empty;

    public string EstadoPago { get; set; } =
        string.Empty;

    public string? ReferenciaExterna { get; set; }

    public DateTime? FechaPago { get; set; }

    public DateTime FechaCreacion { get; set; }
}
