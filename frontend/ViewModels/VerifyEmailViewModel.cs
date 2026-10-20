using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class VerifyEmailViewModel
{
    [Required(
        ErrorMessage = "El token de verificacion es obligatorio.")]
    [StringLength(
        500,
        ErrorMessage = "El token no puede superar 500 caracteres.")]
    [Display(Name = "Token de verificacion")]
    public string Token { get; set; } =
        string.Empty;
}