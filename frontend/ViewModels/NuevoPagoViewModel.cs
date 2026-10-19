using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class NuevoPagoViewModel
{
    public int IdReserva { get; set; }

    public string CodigoReserva { get; set; } =
        string.Empty;

    public string EstadoReserva { get; set; } =
        string.Empty;

    public decimal MontoTotal { get; set; }

    public decimal MontoDeposito { get; set; }

    [Required(
        ErrorMessage = "El monto es obligatorio.")]
    [Range(
        typeof(decimal),
        "0.01",
        "9999999999.99",
        ErrorMessage = "El monto debe ser mayor que 0.")]
    public decimal? Monto { get; set; }

    [Required(
        ErrorMessage = "El metodo de pago es obligatorio.")]
    [StringLength(
        50,
        ErrorMessage = "El metodo de pago no puede superar 50 caracteres.")]
    public string MetodoPago { get; set; } =
        string.Empty;
}
