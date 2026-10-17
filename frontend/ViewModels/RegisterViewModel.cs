using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class RegisterViewModel
{
    [Required(
        ErrorMessage = "Los nombres son obligatorios.")]
    [StringLength(
        100,
        ErrorMessage = "Los nombres no pueden superar 100 caracteres.")]
    [Display(
        Name = "Nombres")]
    public string Nombres { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "Los apellidos son obligatorios.")]
    [StringLength(
        100,
        ErrorMessage = "Los apellidos no pueden superar 100 caracteres.")]
    [Display(
        Name = "Apellidos")]
    public string Apellidos { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "El correo es obligatorio.")]
    [EmailAddress(
        ErrorMessage = "El correo no tiene un formato valido.")]
    [StringLength(
        254,
        ErrorMessage = "El correo no puede superar 254 caracteres.")]
    [Display(
        Name = "Correo")]
    public string Correo { get; set; } =
        string.Empty;

    [StringLength(
        20,
        ErrorMessage = "El telefono no puede superar 20 caracteres.")]
    [Display(
        Name = "Telefono")]
    public string? Telefono { get; set; }

    [Required(
        ErrorMessage = "La contrasena es obligatoria.")]
    [StringLength(
        72,
        MinimumLength = 8,
        ErrorMessage = "La contrasena debe tener entre 8 y 72 caracteres.")]
    [DataType(
        DataType.Password)]
    [Display(
        Name = "Contrasena")]
    public string Password { get; set; } =
        string.Empty;

    [Required(
        ErrorMessage = "Confirma tu contrasena.")]
    [DataType(
        DataType.Password)]
    [Compare(
        nameof(Password),
        ErrorMessage = "Las contrasenas no coinciden.")]
    [Display(
        Name = "Confirmar contrasena")]
    public string ConfirmPassword { get; set; } =
        string.Empty;
}
