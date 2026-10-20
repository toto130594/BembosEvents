using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.ViewModels;

public sealed class MfaChallengeViewModel
{
    [Display(Name = "Codigo de autenticacion")]
    [RegularExpression(
        @"^\d{6}$",
        ErrorMessage = "Ingresa un codigo de 6 digitos.")]
    public string? Code { get; set; }

    [Display(Name = "Codigo de recuperacion")]
    [StringLength(
        100,
        ErrorMessage = "El codigo de recuperacion no es valido.")]
    public string? RecoveryCode { get; set; }
}