using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class ReservationTematicaViewModel
{
    public int? IdTematica { get; set; }

    public IReadOnlyList<TematicaPublicaDto> Tematicas { get; set; } =
        Array.Empty<TematicaPublicaDto>();
}
