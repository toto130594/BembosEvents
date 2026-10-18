namespace BembosEvents.Web.ViewModels;

public sealed class ReservaDetalleViewModel
{
    public int IdReserva { get; set; }

    public string CodigoReserva { get; set; } =
        string.Empty;

    public string Estado { get; set; } =
        string.Empty;

    public string SedeNombre { get; set; } =
        string.Empty;

    public string SalonNombre { get; set; } =
        string.Empty;

    public string? PaqueteNombre { get; set; }

    public string? TematicaNombre { get; set; }

    public DateOnly FechaEvento { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public int CantidadInvitados { get; set; }

    public decimal MontoBase { get; set; }

    public decimal MontoServicios { get; set; }

    public decimal MontoTotal { get; set; }

    public decimal PorcentajeDeposito { get; set; }

    public decimal MontoDeposito { get; set; }

    public DateTime FechaCreacion { get; set; }

    public DateTime FechaActualizacion { get; set; }

    public IReadOnlyList<ReservaDetalleServicioViewModel> Servicios
    {
        get;
        set;
    } = Array.Empty<ReservaDetalleServicioViewModel>();
}

public sealed class ReservaDetalleServicioViewModel
{
    public int IdServicioAdicional { get; set; }

    public string Nombre { get; set; } =
        string.Empty;

    public int Cantidad { get; set; }

    public decimal PrecioUnitario { get; set; }

    public decimal Subtotal { get; set; }
}
