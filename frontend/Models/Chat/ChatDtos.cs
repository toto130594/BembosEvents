namespace BembosEvents.Web.Models.Chat;

public sealed class ChatConversationSummaryDto
{
    public int IdConversacion { get; init; }

    public int? IdSede { get; init; }

    public string Estado { get; init; } =
        string.Empty;

    public DateTime FechaCreacion { get; init; }

    public DateTime FechaActualizacion { get; init; }

    public DateTime? FechaCierre { get; init; }
}

public sealed class ChatMessageDto
{
    public int IdMensajeChat { get; init; }

    public string TipoEmisor { get; init; } =
        string.Empty;

    public int? IdUsuarioEmisor { get; init; }

    public string Contenido { get; init; } =
        string.Empty;

    public DateTime FechaEnvio { get; init; }
}

public sealed class ChatConversationDetailDto
{
    public int IdConversacion { get; init; }

    public int IdCliente { get; init; }

    public int? IdSede { get; init; }

    public string Estado { get; init; } =
        string.Empty;

    public DateTime FechaCreacion { get; init; }

    public DateTime FechaActualizacion { get; init; }

    public DateTime? FechaCierre { get; init; }

    public List<ChatMessageDto> Mensajes { get; init; } =
        new();
}

public sealed class CreateChatConversationRequestDto
{
    public int? IdSede { get; init; }
}

public sealed class SendChatMessageRequestDto
{
    public string Contenido { get; init; } =
        string.Empty;
}

public sealed class ChatTurnResponseDto
{
    public ChatMessageDto? Cliente { get; init; }

    public ChatMessageDto? Ia { get; init; }
}