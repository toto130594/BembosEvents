namespace BembosEvents.Web.Models.Reservations;

public sealed class ReservationDraft
{
    public int? IdSede { get; set; }

    public int? IdSalon { get; set; }

    public DateOnly? FechaEvento { get; set; }

    public TimeOnly? HoraInicio { get; set; }

    public TimeOnly? HoraFin { get; set; }

    public int? CantidadInvitados { get; set; }

    public int? IdPaquete { get; set; }

    public bool PaqueteCompletado { get; set; }

    public int? IdTematica { get; set; }

    public bool TematicaCompletada { get; set; }

    public bool ServiciosCompletados { get; set; }

    public List<ReservationServiceDraft> Servicios { get; set; } =
        new();
}

public sealed class ReservationServiceDraft
{
    public int IdServicioAdicional { get; set; }

    public int Cantidad { get; set; }
}
