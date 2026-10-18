using System.ComponentModel.DataAnnotations;
using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationSedeViewModel
{
    [Required(
        ErrorMessage = "Selecciona una sede.")]
    [Range(
        1,
        int.MaxValue,
        ErrorMessage = "Selecciona una sede valida.")]
    public int? IdSede { get; set; }

    public IReadOnlyList<SedePublicaDto> Sedes { get; set; } =
        Array.Empty<SedePublicaDto>();
}
