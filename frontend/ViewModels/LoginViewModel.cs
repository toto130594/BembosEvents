using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class LoginViewModel
{
    [Required(
        ErrorMessage = "El correo es obligatorio.")]
    [EmailAddress(
        ErrorMessage = "Ingresa un correo valido.")]
    [Display(
        Name = "Correo")]
    public string Correo { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "La contrasena es obligatoria.")]
    [DataType(
        DataType.Password)]
    [Display(
        Name = "Contrasena")]
    public string Password { get; set; } =
        string.Empty;

    public string? ReturnUrl { get; set; }
}
