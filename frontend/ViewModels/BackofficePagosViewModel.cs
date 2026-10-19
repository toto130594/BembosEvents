using BembosEvents.Web.Models.Api;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficePagosViewModel
{
    public IReadOnlyList<PagoDto> Pagos { get; set; } =
        Array.Empty<PagoDto>();
}
