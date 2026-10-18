namespace BembosEvents.Web.Models.Api;

public sealed class DisponibilidadSalonDto
{
    public int IdSalon { get; set; }

    public DateOnly Fecha { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public bool Disponible { get; set; }

    public string? Motivo { get; set; }
}
