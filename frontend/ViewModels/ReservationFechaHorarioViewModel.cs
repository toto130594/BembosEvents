using System.ComponentModel.DataAnnotations;
using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationFechaHorarioViewModel
{
    public int IdSalon { get; set; }

    public string SalonNombre { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "Selecciona la fecha del evento.")]
    public DateOnly? FechaEvento { get; set; }

    [Required(
        ErrorMessage = "Selecciona la hora de inicio.")]
    public TimeOnly? HoraInicio { get; set; }

    [Required(
        ErrorMessage = "Selecciona la hora de fin.")]
    public TimeOnly? HoraFin { get; set; }

    public IReadOnlyList<HorarioSalonDto> Horarios { get; set; } =
        Array.Empty<HorarioSalonDto>();
}
