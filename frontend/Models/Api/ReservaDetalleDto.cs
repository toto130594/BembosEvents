namespace BembosEvents.Web.Models.Api;

public sealed class ReservaDetalleDto
{
    public int IdReserva { get; set; }

    public string CodigoReserva { get; set; } =
        string.Empty;

    public string Estado { get; set; } =
        string.Empty;

    public int IdSede { get; set; }

    public int IdSalon { get; set; }

    public int? IdPaquete { get; set; }

    public int? IdTematica { get; set; }

    public DateOnly FechaEvento { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public int CantidadInvitados { get; set; }

    public decimal MontoBase { get; set; }

    public decimal MontoServicios { get; set; }

    public decimal MontoTotal { get; set; }

    public decimal PorcentajeDeposito { get; set; }

    public decimal MontoDeposito { get; set; }

    public List<ReservaDetalleServicioDto> ServiciosAdicionales
    {
        get;
        set;
    } = new();

    public DateTime FechaCreacion { get; set; }

    public DateTime FechaActualizacion { get; set; }
}

public sealed class ReservaDetalleServicioDto
{
    public int IdServicioAdicional { get; set; }

    public int Cantidad { get; set; }

    public decimal PrecioUnitario { get; set; }

    public decimal Subtotal { get; set; }
}
