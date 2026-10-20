using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class ForgotPasswordViewModel
{
    [Required(
        ErrorMessage = "El correo es obligatorio.")]
    [EmailAddress(
        ErrorMessage = "Ingresa un correo valido.")]
    [StringLength(
        254,
        ErrorMessage = "El correo no puede superar 254 caracteres.")]
    [Display(Name = "Correo")]
    public string Correo { get; set; } =
        string.Empty;
}