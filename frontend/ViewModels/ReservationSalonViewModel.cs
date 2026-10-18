using System.ComponentModel.DataAnnotations;
using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationSalonViewModel
{
    public int IdSede { get; set; }

    public string SedeNombre { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "Selecciona un salon.")]
    [Range(
        1,
        int.MaxValue,
        ErrorMessage = "Selecciona un salon valido.")]
    public int? IdSalon { get; set; }

    public IReadOnlyList<SalonPublicoDto> Salones { get; set; } =
        Array.Empty<SalonPublicoDto>();
}
