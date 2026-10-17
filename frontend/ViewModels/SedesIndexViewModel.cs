using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class SedesIndexViewModel
{
    public IReadOnlyList<SedePublicaDto> Sedes { get; init; } =
        Array.Empty<SedePublicaDto>();
}
