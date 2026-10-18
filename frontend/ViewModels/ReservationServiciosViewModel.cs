using System.ComponentModel.DataAnnotations;
using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationServiciosViewModel
{
    public IReadOnlyList<ServicioAdicionalPublicoDto> Catalogo { get; set; } =
        Array.Empty<ServicioAdicionalPublicoDto>();

    public List<ReservationServicioSeleccionViewModel> Servicios
    {
        get;
        set;
    } = new();
}

public sealed class ReservationServicioSeleccionViewModel
{
    public int IdServicioAdicional { get; set; }

    [Range(
        0,
        int.MaxValue,
        ErrorMessage = "La cantidad no puede ser negativa.")]
    public int Cantidad { get; set; }
}
