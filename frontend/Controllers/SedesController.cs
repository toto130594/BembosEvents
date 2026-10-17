using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("sedes")]
public sealed class SedesController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public SedesController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var sedes =
            await _backendApiClient.GetSedesAsync(
                cancellationToken);

        return View(
            new SedesIndexViewModel
            {
                Sedes =
                    sedes
            });
    }

    [HttpGet("{idSede:int}")]
    public async Task<IActionResult> Details(
        int idSede,
        CancellationToken cancellationToken)
    {
        if (idSede <= 0)
        {
            return NotFound();
        }

        var sede =
            await _backendApiClient.GetSedeAsync(
                idSede,
                cancellationToken);

        if (sede is null)
        {
            return NotFound();
        }

        var salones =
            await _backendApiClient.GetSalonesBySedeAsync(
                idSede,
                cancellationToken);

        return View(
            new SedeDetailsViewModel
            {
                Sede =
                    sede,

                Salones =
                    salones
                        .Where(
                            salon =>
                                salon.IdSede ==
                                idSede)
                        .ToArray()
            });
    }
}
