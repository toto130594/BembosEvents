using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("salones")]
public sealed class SalonesController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public SalonesController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("{idSalon:int}")]
    public async Task<IActionResult> Details(
        int idSalon,
        CancellationToken cancellationToken)
    {
        if (idSalon <= 0)
        {
            return NotFound();
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                idSalon,
                cancellationToken);

        if (salon is null)
        {
            return NotFound();
        }

        var horarios =
            await _backendApiClient.GetHorariosSalonAsync(
                idSalon,
                cancellationToken);

        var sede =
            await _backendApiClient.GetSedeAsync(
                salon.IdSede,
                cancellationToken);

        return View(
            new SalonDetailsViewModel
            {
                Salon =
                    salon,

                Sede =
                    sede,

                Horarios =
                    horarios
                        .Where(
                            horario =>
                                horario.IdSalon ==
                                idSalon)
                        .ToArray()
            });
    }
}
