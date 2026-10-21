using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[Authorize]
[Route("chat")]
public sealed class ChatController : Controller
{
    private readonly IChatApiClient _chatApiClient;
    private readonly IBackendApiClient _backendApiClient;

    public ChatController(
        IChatApiClient chatApiClient,
        IBackendApiClient backendApiClient)
    {
        _chatApiClient =
            chatApiClient;

        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var result =
            await _chatApiClient
                .GetClientConversationsAsync(
                    cancellationToken);

        if (!result.Success)
        {
            return HandleBackendFailure(
                result.StatusCode);
        }

        var sedes =
            await _backendApiClient
                .GetSedesAsync(
                    cancellationToken);

        return View(
            new ChatIndexViewModel
            {
                Conversaciones =
                    result.Data ??
                    new(),

                Sedes =
                    sedes
            });
    }

    [HttpPost("nueva")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Create(
        int? idSede,
        CancellationToken cancellationToken)
    {
        if (idSede <= 0)
        {
            idSede = null;
        }

        var result =
            await _chatApiClient
                .CreateClientConversationAsync(
                    idSede,
                    cancellationToken);

        if (!result.Success)
        {
            TempData["ChatError"] =
                "No fue posible crear la conversacion.";

            return HandleBackendFailure(
                result.StatusCode,
                redirectToIndexOnServerError: true);
        }

        if (result.Data is null)
        {
            TempData["ChatError"] =
                "Spring Boot no devolvio la conversacion creada.";

            return RedirectToAction(
                nameof(Index));
        }

        return RedirectToAction(
            nameof(Detail),
            new
            {
                idConversacion =
                    result.Data.IdConversacion
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
                .GetClientConversationAsync(
                    idConversacion,
                    cancellationToken);

        if (!result.Success)
        {
            return HandleBackendFailure(
                result.StatusCode);
        }

        if (result.Data is null)
        {
            return NotFound();
        }

        return View(
            new ChatDetailViewModel
            {
                Conversacion =
                    result.Data
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
                .SendClientMessageAsync(
                    idConversacion,
                    contenido,
                    cancellationToken);

        if (!result.Success)
        {
            TempData["ChatError"] =
                "No fue posible enviar el mensaje.";

            return HandleBackendFailure(
                result.StatusCode,
                idConversacion: idConversacion);
        }

        return RedirectToAction(
            nameof(Detail),
            new
            {
                idConversacion
            });
    }

    [HttpPost("{idConversacion:int}/escalar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Escalate(
        int idConversacion,
        CancellationToken cancellationToken)
    {
        var result =
            await _chatApiClient
                .EscalateClientConversationAsync(
                    idConversacion,
                    cancellationToken);

        if (!result.Success)
        {
            TempData["ChatError"] =
                result.StatusCode == 400
                    ? "Esta conversacion necesita una sede antes de solicitar un encargado."
                    : "No fue posible solicitar un encargado.";

            return HandleBackendFailure(
                result.StatusCode,
                idConversacion: idConversacion);
        }

        TempData["ChatSuccess"] =
            "Solicitud enviada. Un encargado de la sede podra tomar la conversacion.";

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
                .GetClientHistoryAsync(
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

    private IActionResult HandleBackendFailure(
        int statusCode,
        bool redirectToIndexOnServerError = false,
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

        if (redirectToIndexOnServerError)
        {
            return RedirectToAction(
                nameof(Index));
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