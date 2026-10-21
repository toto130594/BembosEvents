using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficeChatIndexViewModel
{
    public IReadOnlyList<ChatConversationSummaryDto> Conversaciones { get; init; } =
        Array.Empty<ChatConversationSummaryDto>();

    public IReadOnlyList<ChatConversationSummaryDto> Pendientes { get; init; } =
        Array.Empty<ChatConversationSummaryDto>();
}