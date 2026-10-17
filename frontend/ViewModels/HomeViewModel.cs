using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class HomeViewModel
{
    public IReadOnlyList<SedePublicaDto> Sedes { get; init; } =
        Array.Empty<SedePublicaDto>();

    public int PaquetesCount { get; init; }

    public int TematicasCount { get; init; }

    public int ServiciosAdicionalesCount { get; init; }

    public bool SedesApiAvailable { get; init; }

    public bool PaquetesApiAvailable { get; init; }

    public bool TematicasApiAvailable { get; init; }

    public bool ServiciosApiAvailable { get; init; }

    public bool HasPartialLoadError =>
        !SedesApiAvailable ||
        !PaquetesApiAvailable ||
        !TematicasApiAvailable ||
        !ServiciosApiAvailable;
}
