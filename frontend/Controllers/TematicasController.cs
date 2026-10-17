using BembosEvents.Web.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("tematicas")]
public sealed class TematicasController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public TematicasController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var tematicas =
            await _backendApiClient.GetTematicasAsync(
                cancellationToken);

        return View(
            tematicas);
    }

    [HttpGet("{idTematica:int}")]
    public async Task<IActionResult> Details(
        int idTematica,
        CancellationToken cancellationToken)
    {
        if (idTematica <= 0)
        {
            return NotFound();
        }

        var tematica =
            await _backendApiClient.GetTematicaAsync(
                idTematica,
                cancellationToken);

        if (tematica is null)
        {
            return NotFound();
        }

        return View(
            tematica);
    }
}
