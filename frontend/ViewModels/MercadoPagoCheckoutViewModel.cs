using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class MercadoPagoCheckoutViewModel
{
    [Range(
        1,
        int.MaxValue,
        ErrorMessage = "La reserva es obligatoria.")]
    [Display(Name = "Reserva")]
    public int IdReserva { get; set; }

    [Range(
        typeof(decimal),
        "0.01",
        "9999999999.99",
        ErrorMessage = "El monto debe ser mayor que cero.")]
    [Display(Name = "Monto")]
    public decimal Monto { get; set; }
}