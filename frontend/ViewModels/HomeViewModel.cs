using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class HomeViewModel
{
    public IReadOnlyList<SedePublicaDto> Sedes { get; init; } =
        Array.Empty<SedePublicaDto>();

    public IReadOnlyList<PaquetePublicoDto> Paquetes { get; init; } =
        Array.Empty<PaquetePublicoDto>();

    public IReadOnlyList<TematicaPublicaDto> Tematicas { get; init; } =
        Array.Empty<TematicaPublicaDto>();

    public IReadOnlyList<ServicioAdicionalPublicoDto> ServiciosAdicionales { get; init; } =
        Array.Empty<ServicioAdicionalPublicoDto>();

    public int PaquetesCount => Paquetes.Count;

    public int TematicasCount => Tematicas.Count;

    public int ServiciosAdicionalesCount => ServiciosAdicionales.Count;

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