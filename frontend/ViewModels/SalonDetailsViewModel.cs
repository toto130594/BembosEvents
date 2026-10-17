using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class SalonDetailsViewModel
{
    public required SalonPublicoDto Salon { get; init; }

    public SedePublicaDto? Sede { get; init; }

    public IReadOnlyList<HorarioSalonDto> Horarios { get; init; } =
        Array.Empty<HorarioSalonDto>();
}
