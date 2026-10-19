using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficeReservasViewModel
{
    public IReadOnlyList<ReservaBackofficeResumenDto> Reservas { get; set; } =
        Array.Empty<ReservaBackofficeResumenDto>();
}
