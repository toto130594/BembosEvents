using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.ViewModels;

public sealed class BackofficeChatDetailViewModel
{
    public required ChatConversationDetailDto Conversacion { get; init; }
}