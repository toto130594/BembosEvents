using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class ResetPasswordViewModel
{
    [Required(
        ErrorMessage = "El token de recuperacion es obligatorio.")]
    [StringLength(
        500,
        ErrorMessage = "El token no puede superar 500 caracteres.")]
    [Display(Name = "Token de recuperacion")]
    public string Token { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "La nueva contrasena es obligatoria.")]
    [StringLength(
        100,
        MinimumLength = 8,
        ErrorMessage = "La contrasena debe tener entre 8 y 100 caracteres.")]
    [DataType(DataType.Password)]
    [Display(Name = "Nueva contrasena")]
    public string NuevaPassword { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "Confirma la nueva contrasena.")]
    [DataType(DataType.Password)]
    [Compare(
        nameof(NuevaPassword),
        ErrorMessage = "Las contrasenas no coinciden.")]
    [Display(Name = "Confirmar contrasena")]
    public string ConfirmarPassword { get; set; } =
        string.Empty;
}