using BembosEvents.Web.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("servicios")]
public sealed class ServiciosController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public ServiciosController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var servicios =
            await _backendApiClient.GetServiciosAdicionalesAsync(
                cancellationToken);

        return View(
            servicios);
    }

    [HttpGet("{idServicioAdicional:int}")]
    public async Task<IActionResult> Details(
        int idServicioAdicional,
        CancellationToken cancellationToken)
    {
        if (idServicioAdicional <= 0)
        {
            return NotFound();
        }

        var servicio =
            await _backendApiClient.GetServicioAdicionalAsync(
                idServicioAdicional,
                cancellationToken);

        if (servicio is null)
        {
            return NotFound();
        }

        return View(
            servicio);
    }
}
