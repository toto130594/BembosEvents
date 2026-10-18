using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationPaqueteViewModel
{
    public int? IdPaquete { get; set; }

    public IReadOnlyList<PaquetePublicoDto> Paquetes { get; set; } =
        Array.Empty<PaquetePublicoDto>();
}
