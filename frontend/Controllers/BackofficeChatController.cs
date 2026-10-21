using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[BackofficeAuthorize]
[Route("backoffice/chat")]
public sealed class BackofficeChatController : Controller
{
    private readonly IChatApiClient _chatApiClient;

    public BackofficeChatController(
        IChatApiClient chatApiClient)
    {
        _chatApiClient =
            chatApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var conversations =
            await _chatApiClient
                .GetBackofficeConversationsAsync(
                    cancellationToken);

        if (!conversations.Success)
        {
            return HandleFailure(
                conversations.StatusCode);
        }

        var pending =
            await _chatApiClient
                .GetBackofficePendingAsync(
                    cancellationToken);

        if (!pending.Success)
        {
            return HandleFailure(
                pending.StatusCode);
        }

        return View(
            new BackofficeChatIndexViewModel
            {
                Conversaciones =
                    conversations.Data ??
                    new(),

                Pendientes =
                    pending.Data ??
                    new()
            });
    }

    [HttpGet("{idConversacion:int}")]
    public async Task<IActionResult> Detail(
        int idConversacion,
        CancellationToken cancellationToken)
    {
        if (idConversacion <= 0)
        {
            return NotFound();
        }

        var result =
            await _chatApiClient
                .GetBackofficeConversationAsync(
                    idConversacion,
                    cancellationToken);

        if (!result.Success)
        {
            return HandleFailure(
                result.StatusCode);
        }

        if (result.Data is null)
        {
            return NotFound();
        }

        return View(
            new BackofficeChatDetailViewModel
            {
                Conversacion =
                    result.Data
            });
    }

    [HttpPost("{idConversacion:int}/tomar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Take(
        int idConversacion,
        CancellationToken cancellationToken)
    {
        var result =
            await _chatApiClient
                .TakeBackofficeConversationAsync(
                    idConversacion,
                    cancellationToken);

        if (!result.Success)
        {
            TempData["ChatError"] =
                result.StatusCode == 403
                    ? "Solo el encargado de la sede puede tomar esta conversacion."
                    : "No fue posible tomar la conversacion.";

            return HandleFailure(
                result.StatusCode,
                idConversacion);
        }

        TempData["ChatSuccess"] =
            "Conversacion tomada correctamente.";

        return RedirectToAction(
            nameof(Detail),
            new
            {
                idConversacion
            });
    }

    [HttpPost("{idConversacion:int}/mensaje")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> SendMessage(
        int idConversacion,
        string contenido,
        CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(contenido))
        {
            TempData["ChatError"] =
                "Escribe un mensaje.";

            return RedirectToAction(
                nameof(Detail),
                new
                {
                    idConversacion
                });
        }

        var result =
            await _chatApiClient
                .SendBackofficeMessageAsync(
                    idConversacion,
                    contenido,
                    cancellationToken);

        if (!result.Success)
        {
            TempData["ChatError"] =
                result.StatusCode == 403
                    ? "No tienes alcance sobre esta conversacion."
                    : "No fue posible enviar el mensaje.";

            return HandleFailure(
                result.StatusCode,
                idConversacion);
        }

        return RedirectToAction(
            nameof(Detail),
            new
            {
                idConversacion
            });
    }

    [HttpGet("{idConversacion:int}/historial.json")]
    public async Task<IActionResult> HistoryJson(
        int idConversacion,
        CancellationToken cancellationToken)
    {
        var result =
            await _chatApiClient
                .GetBackofficeHistoryAsync(
                    idConversacion,
                    cancellationToken);

        if (!result.Success)
        {
            return StatusCode(
                result.StatusCode);
        }

        return Json(
            result.Data ??
            new());
    }

    private IActionResult HandleFailure(
        int statusCode,
        int? idConversacion = null)
    {
        if (statusCode == 401)
        {
            HttpContext.Session.Clear();

            return RedirectToAction(
                "Login",
                "Account",
                new
                {
                    returnUrl =
                        Request.Path.Value
                });
        }

        if (statusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (statusCode == 404)
        {
            return NotFound();
        }

        if (idConversacion.HasValue)
        {
            return RedirectToAction(
                nameof(Detail),
                new
                {
                    idConversacion =
                        idConversacion.Value
                });
        }

        return StatusCode(
            statusCode >= 400 &&
            statusCode <= 599
                ? statusCode
                : 502);
    }
}