namespace BembosEvents.Web.ViewModels;

public sealed class ReservationResumenViewModel
{
    public string SedeNombre { get; set; } =
        string.Empty;

    public string SalonNombre { get; set; } =
        string.Empty;

    public DateOnly FechaEvento { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public int CantidadInvitados { get; set; }

    public string? PaqueteNombre { get; set; }

    public decimal? PaquetePrecioBase { get; set; }

    public string? TematicaNombre { get; set; }

    public decimal? TematicaPrecioAdicional { get; set; }

    public List<ReservationResumenServicioViewModel> Servicios
    {
        get;
        set;
    } = new();
}

public sealed class ReservationResumenServicioViewModel
{
    public string Nombre { get; set; } =
        string.Empty;

    public int Cantidad { get; set; }

    public decimal PrecioUnitario { get; set; }
}

public sealed class ReservationConfirmacionViewModel
{
    public int IdReserva { get; set; }
}
