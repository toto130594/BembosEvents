using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.ViewModels;

public sealed class ChatIndexViewModel
{
    public IReadOnlyList<ChatConversationSummaryDto> Conversaciones { get; init; } =
        Array.Empty<ChatConversationSummaryDto>();

    public IReadOnlyList<SedePublicaDto> Sedes { get; init; } =
        Array.Empty<SedePublicaDto>();
}