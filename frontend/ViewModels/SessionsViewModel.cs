using BembosEvents.Web.Models.Auth;

namespace BembosEvents.Web.ViewModels;

public sealed class SessionsViewModel
{
    public IReadOnlyList<BackendSessionDto> Sessions { get; init; } =
        Array.Empty<BackendSessionDto>();

    public string? ErrorMessage { get; init; }
}