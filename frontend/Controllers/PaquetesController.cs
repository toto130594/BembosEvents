using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("paquetes")]
public sealed class PaquetesController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public PaquetesController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient = backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var paquetes =
            await _backendApiClient.GetPaquetesAsync(
                cancellationToken);

        return View(paquetes);
    }

    [HttpGet("{idPaquete:int}")]
    public async Task<IActionResult> Details(
        int idPaquete,
        CancellationToken cancellationToken)
    {
        if (idPaquete <= 0)
        {
            return NotFound();
        }

        var paquete =
            await _backendApiClient.GetPaqueteAsync(
                idPaquete,
                cancellationToken);

        if (paquete is null)
        {
            return NotFound();
        }

        var todos =
            await _backendApiClient.GetPaquetesAsync(
                cancellationToken);

        var relacionados =
            todos
                .Where(item => item.IdPaquete != idPaquete)
                .Take(3)
                .ToArray();

        return View(
            new PaqueteDetailsViewModel
            {
                Paquete = paquete,
                Relacionados = relacionados
            });
    }
}