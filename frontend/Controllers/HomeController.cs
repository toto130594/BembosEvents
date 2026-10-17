using System.Diagnostics;
using System.Text.Json;
using BembosEvents.Web.Models;
using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

public sealed class HomeController : Controller
{
    private readonly IBackendApiClient _backendApiClient;
    private readonly ILogger<HomeController> _logger;

    public HomeController(
        IBackendApiClient backendApiClient,
        ILogger<HomeController> logger)
    {
        _backendApiClient = backendApiClient;
        _logger = logger;
    }

    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var sedes =
            await LoadOrDefaultAsync(
                () =>
                    _backendApiClient.GetSedesAsync(
                        cancellationToken),
                Array.Empty<SedePublicaDto>(),
                "sedes",
                cancellationToken);

        var paquetes =
            await LoadOrDefaultAsync(
                () =>
                    _backendApiClient.GetPaquetesCountAsync(
                        cancellationToken),
                0,
                "paquetes",
                cancellationToken);

        var tematicas =
            await LoadOrDefaultAsync(
                () =>
                    _backendApiClient.GetTematicasCountAsync(
                        cancellationToken),
                0,
                "tematicas",
                cancellationToken);

        var servicios =
            await LoadOrDefaultAsync(
                () =>
                    _backendApiClient.GetServiciosAdicionalesCountAsync(
                        cancellationToken),
                0,
                "servicios adicionales",
                cancellationToken);

        var model =
            new HomeViewModel
            {
                Sedes = sedes.Value,
                SedesApiAvailable = sedes.Success,

                PaquetesCount = paquetes.Value,
                PaquetesApiAvailable = paquetes.Success,

                TematicasCount = tematicas.Value,
                TematicasApiAvailable = tematicas.Success,

                ServiciosAdicionalesCount = servicios.Value,
                ServiciosApiAvailable = servicios.Success
            };

        return View(model);
    }

    public IActionResult Privacy()
    {
        return View();
    }

    [ResponseCache(
        Duration = 0,
        Location = ResponseCacheLocation.None,
        NoStore = true)]
    public IActionResult Error()
    {
        return View(
            new ErrorViewModel
            {
                RequestId =
                    Activity.Current?.Id ??
                    HttpContext.TraceIdentifier
            });
    }

    private async Task<LoadResult<T>> LoadOrDefaultAsync<T>(
        Func<Task<T>> loader,
        T fallback,
        string resource,
        CancellationToken cancellationToken)
    {
        try
        {
            var value =
                await loader();

            return new LoadResult<T>(
                value,
                true);
        }
        catch (OperationCanceledException)
            when (!cancellationToken.IsCancellationRequested)
        {
            _logger.LogWarning(
                "Timeout al cargar {Resource} para Home.",
                resource);

            return new LoadResult<T>(
                fallback,
                false);
        }
        catch (HttpRequestException exception)
        {
            _logger.LogWarning(
                exception,
                "No se pudo cargar {Resource} para Home.",
                resource);

            return new LoadResult<T>(
                fallback,
                false);
        }
        catch (JsonException exception)
        {
            _logger.LogWarning(
                exception,
                "JSON invalido al cargar {Resource} para Home.",
                resource);

            return new LoadResult<T>(
                fallback,
                false);
        }
    }

    private sealed record LoadResult<T>(
        T Value,
        bool Success);
}
