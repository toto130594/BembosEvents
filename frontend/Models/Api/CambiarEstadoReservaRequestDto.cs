using System.ComponentModel.DataAnnotations;

namespace BembosEvents.Web.Models.Api;

public sealed class CambiarEstadoReservaRequestDto
{
    [Required(
        ErrorMessage = "El nuevo estado es obligatorio.")]
    public string NuevoEstado { get; set; } =
        string.Empty;
}
