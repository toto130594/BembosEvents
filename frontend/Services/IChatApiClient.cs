using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.Services;

public interface IChatApiClient
{
    Task<ChatApiResult<List<ChatConversationSummaryDto>>>
        GetClientConversationsAsync(
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatConversationDetailDto>>
        CreateClientConversationAsync(
            int? idSede,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatConversationDetailDto>>
        GetClientConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<List<ChatMessageDto>>>
        GetClientHistoryAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatTurnResponseDto>>
        SendClientMessageAsync(
            int idConversacion,
            string contenido,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatConversationSummaryDto>>
        EscalateClientConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<List<ChatConversationSummaryDto>>>
        GetBackofficeConversationsAsync(
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<List<ChatConversationSummaryDto>>>
        GetBackofficePendingAsync(
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatConversationDetailDto>>
        GetBackofficeConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<List<ChatMessageDto>>>
        GetBackofficeHistoryAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatConversationSummaryDto>>
        TakeBackofficeConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default);

    Task<ChatApiResult<ChatMessageDto>>
        SendBackofficeMessageAsync(
            int idConversacion,
            string contenido,
            CancellationToken cancellationToken = default);
}