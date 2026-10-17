namespace BembosEvents.Web.Models.Api;

public sealed class HorarioSalonDto
{
    public int IdHorarioSalon { get; init; }

    public int IdSalon { get; init; }

    public string DiaSemana { get; init; } =
        string.Empty;

    public TimeSpan HoraInicio { get; init; }

    public TimeSpan HoraFin { get; init; }
}
