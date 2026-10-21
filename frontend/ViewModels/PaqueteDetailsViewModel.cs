using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class PaqueteDetailsViewModel
{
    public required PaquetePublicoDto Paquete { get; init; }

    public IReadOnlyList<PaquetePublicoDto> Relacionados { get; init; } =
        Array.Empty<PaquetePublicoDto>();
}