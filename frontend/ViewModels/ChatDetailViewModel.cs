using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.ViewModels;

public sealed class ChatDetailViewModel
{
    public required ChatConversationDetailDto Conversacion { get; init; }
}