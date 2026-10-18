using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationInvitadosViewModel
{
    public string SalonNombre { get; set; } =
        string.Empty;

    public int CapacidadSalon { get; set; }

    [Required(
        ErrorMessage = "Indica la cantidad de invitados.")]
    [Range(
        1,
        int.MaxValue,
        ErrorMessage = "La cantidad de invitados debe ser mayor a cero.")]
    public int? CantidadInvitados { get; set; }
}
