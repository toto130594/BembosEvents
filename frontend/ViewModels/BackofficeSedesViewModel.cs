using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficeSedesViewModel
{
    public IReadOnlyList<SedeBackofficeDto> Sedes { get; set; } =
        Array.Empty<SedeBackofficeDto>();
}
