using System.Net.Http.Json;
using System.Text.Json;
using BembosEvents.Web.Models.Chat;

namespace BembosEvents.Web.Services;

public sealed class ChatApiClient
    : IChatApiClient
{
    private static readonly JsonSerializerOptions JsonOptions =
        new()
        {
            PropertyNameCaseInsensitive = true
        };

    private readonly HttpClient _httpClient;

    public ChatApiClient(
        HttpClient httpClient)
    {
        _httpClient =
            httpClient;
    }

    public async Task<
        ChatApiResult<List<ChatConversationSummaryDto>>>
        GetClientConversationsAsync(
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/chat/conversaciones",
                cancellationToken);

        return await ReadAsync<
            List<ChatConversationSummaryDto>>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatConversationDetailDto>>
        CreateClientConversationAsync(
            int? idSede,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsJsonAsync(
                "api/chat/conversaciones",
                new CreateChatConversationRequestDto
                {
                    IdSede = idSede
                },
                JsonOptions,
                cancellationToken);

        return await ReadAsync<
            ChatConversationDetailDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatConversationDetailDto>>
        GetClientConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/chat/conversaciones/{idConversacion}",
                cancellationToken);

        return await ReadAsync<
            ChatConversationDetailDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<List<ChatMessageDto>>>
        GetClientHistoryAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/chat/conversaciones/{idConversacion}/historial",
                cancellationToken);

        return await ReadAsync<
            List<ChatMessageDto>>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatTurnResponseDto>>
        SendClientMessageAsync(
            int idConversacion,
            string contenido,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsJsonAsync(
                $"api/chat/conversaciones/{idConversacion}/mensajes",
                new SendChatMessageRequestDto
                {
                    Contenido =
                        contenido.Trim()
                },
                JsonOptions,
                cancellationToken);

        return await ReadAsync<
            ChatTurnResponseDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatConversationSummaryDto>>
        EscalateClientConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                $"api/chat/conversaciones/{idConversacion}/escalar",
                content: null,
                cancellationToken);

        return await ReadAsync<
            ChatConversationSummaryDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<List<ChatConversationSummaryDto>>>
        GetBackofficeConversationsAsync(
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/backoffice/chat/conversaciones",
                cancellationToken);

        return await ReadAsync<
            List<ChatConversationSummaryDto>>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<List<ChatConversationSummaryDto>>>
        GetBackofficePendingAsync(
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                "api/backoffice/chat/conversaciones/pendientes",
                cancellationToken);

        return await ReadAsync<
            List<ChatConversationSummaryDto>>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatConversationDetailDto>>
        GetBackofficeConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/backoffice/chat/conversaciones/{idConversacion}",
                cancellationToken);

        return await ReadAsync<
            ChatConversationDetailDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<List<ChatMessageDto>>>
        GetBackofficeHistoryAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.GetAsync(
                $"api/backoffice/chat/conversaciones/{idConversacion}/historial",
                cancellationToken);

        return await ReadAsync<
            List<ChatMessageDto>>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatConversationSummaryDto>>
        TakeBackofficeConversationAsync(
            int idConversacion,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsync(
                $"api/backoffice/chat/conversaciones/{idConversacion}/tomar",
                content: null,
                cancellationToken);

        return await ReadAsync<
            ChatConversationSummaryDto>(
                response,
                cancellationToken);
    }

    public async Task<
        ChatApiResult<ChatMessageDto>>
        SendBackofficeMessageAsync(
            int idConversacion,
            string contenido,
            CancellationToken cancellationToken = default)
    {
        using var response =
            await _httpClient.PostAsJsonAsync(
                $"api/backoffice/chat/conversaciones/{idConversacion}/mensajes",
                new SendChatMessageRequestDto
                {
                    Contenido =
                        contenido.Trim()
                },
                JsonOptions,
                cancellationToken);

        return await ReadAsync<
            ChatMessageDto>(
                response,
                cancellationToken);
    }

    private static async Task<
        ChatApiResult<T>>
        ReadAsync<T>(
            HttpResponseMessage response,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            T? data = default;

            if (
                response.Content.Headers.ContentLength is null ||
                response.Content.Headers.ContentLength > 0
            )
            {
                data =
                    await response.Content
                        .ReadFromJsonAsync<T>(
                            JsonOptions,
                            cancellationToken);
            }

            return new ChatApiResult<T>
            {
                StatusCode =
                    (int)response.StatusCode,

                Data =
                    data
            };
        }

        return new ChatApiResult<T>
        {
            StatusCode =
                (int)response.StatusCode,

            ErrorBody =
                await response.Content
                    .ReadAsStringAsync(
                        cancellationToken)
        };
    }
}