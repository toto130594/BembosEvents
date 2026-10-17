using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class SedeDetailsViewModel
{
    public required SedePublicaDto Sede { get; init; }

    public IReadOnlyList<SalonPublicoDto> Salones { get; init; } =
        Array.Empty<SalonPublicoDto>();
}
