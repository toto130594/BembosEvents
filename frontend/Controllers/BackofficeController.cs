using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[Authorize]
[BackofficeAuthorize]
[Route("backoffice")]
public sealed class BackofficeController :
    Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public BackofficeController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var sedesResult =
            await _backendApiClient
                .GetBackofficeSedesAsync(
                    cancellationToken);

        if (
            !sedesResult.Success ||
            sedesResult.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                sedesResult.StatusCode);
        }

        var reservasResult =
            await _backendApiClient
                .GetBackofficeReservasAsync(
                    cancellationToken);

        if (
            !reservasResult.Success ||
            reservasResult.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                reservasResult.StatusCode);
        }

        var pagosResult =
            await _backendApiClient
                .GetBackofficePagosAsync(
                    cancellationToken);

        if (
            !pagosResult.Success ||
            pagosResult.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                pagosResult.StatusCode);
        }

        return View(
            new BackofficeDashboardViewModel
            {
                SedesAccesibles =
                    sedesResult.Data.Count,

                ReservasAccesibles =
                    reservasResult.Data.Count,

                PagosAccesibles =
                    pagosResult.Data.Count
            });
    }

    [HttpGet("sedes")]
    public async Task<IActionResult> Sedes(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient
                .GetBackofficeSedesAsync(
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            new BackofficeSedesViewModel
            {
                Sedes =
                    result.Data
            });
    }

    [HttpGet("sedes/{idSede:int}")]
    public async Task<IActionResult> SedeDetalle(
        [FromRoute] int idSede,
        CancellationToken cancellationToken)
    {
        if (idSede <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient
                .GetBackofficeSedeAsync(
                    idSede,
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            result.Data);
    }

    [HttpGet("reservas")]
    public async Task<IActionResult> Reservas(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient
                .GetBackofficeReservasAsync(
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            new BackofficeReservasViewModel
            {
                Reservas =
                    result.Data
            });
    }

    [HttpGet("reservas/{idReserva:int}")]
    public async Task<IActionResult> ReservaDetalle(
        [FromRoute] int idReserva,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient
                .GetBackofficeReservaAsync(
                    idReserva,
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            CrearReservaDetalleViewModel(
                result.Data,
                mensajeExito:
                    TempData["BackofficeReservaSuccess"]
                    as string));
    }

    [HttpPost("reservas/{idReserva:int}/estado")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> CambiarEstadoReserva(
        [FromRoute] int idReserva,
        CambiarEstadoReservaRequestDto model,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        model.NuevoEstado =
            model.NuevoEstado?
                .Trim()
                .ToUpperInvariant() ??
            string.Empty;

        ModelState.Remove(
            nameof(model.NuevoEstado));

        if (
            string.IsNullOrWhiteSpace(
                model.NuevoEstado)
        )
        {
            ModelState.AddModelError(
                nameof(model.NuevoEstado),
                "El nuevo estado es obligatorio.");
        }

        if (!ModelState.IsValid)
        {
            return await RenderCambioEstadoErrorAsync(
                idReserva,
                "Selecciona un estado valido.",
                cancellationToken);
        }

        var result =
            await _backendApiClient
                .CambiarEstadoReservaBackofficeAsync(
                    idReserva,
                    new CambiarEstadoReservaRequestDto
                    {
                        NuevoEstado =
                            model.NuevoEstado
                    },
                    cancellationToken);

        if (
            result.Success &&
            result.Data is not null
        )
        {
            TempData["BackofficeReservaSuccess"] =
                "Estado de la reserva actualizado correctamente.";

            return RedirectToAction(
                nameof(ReservaDetalle),
                new
                {
                    idReserva
                });
        }

        if (
            result.StatusCode ==
            StatusCodes.Status400BadRequest
        )
        {
            return await RenderCambioEstadoErrorAsync(
                idReserva,
                "El estado solicitado no es valido.",
                cancellationToken);
        }

        if (
            result.StatusCode ==
            StatusCodes.Status409Conflict
        )
        {
            return await RenderCambioEstadoErrorAsync(
                idReserva,
                "La transicion solicitada no esta permitida para el estado actual.",
                cancellationToken);
        }

        return await HandleBackendStatusAsync(
            result.StatusCode);
    }

    [HttpGet("pagos")]
    public async Task<IActionResult> Pagos(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient
                .GetBackofficePagosAsync(
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            new BackofficePagosViewModel
            {
                Pagos =
                    result.Data
            });
    }

    [HttpGet("pagos/{idPago:int}")]
    public async Task<IActionResult> PagoDetalle(
        [FromRoute] int idPago,
        CancellationToken cancellationToken)
    {
        if (idPago <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient
                .GetBackofficePagoAsync(
                    idPago,
                    cancellationToken);

        if (
            !result.Success ||
            result.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                result.StatusCode);
        }

        return View(
            result.Data);
    }

    private async Task<IActionResult> RenderCambioEstadoErrorAsync(
        int idReserva,
        string message,
        CancellationToken cancellationToken)
    {
        var detailResult =
            await _backendApiClient
                .GetBackofficeReservaAsync(
                    idReserva,
                    cancellationToken);

        if (
            !detailResult.Success ||
            detailResult.Data is null
        )
        {
            return await HandleBackendStatusAsync(
                detailResult.StatusCode);
        }

        return View(
            "ReservaDetalle",
            CrearReservaDetalleViewModel(
                detailResult.Data,
                errorCambioEstado:
                    message));
    }

    private static BackofficeReservaDetalleViewModel CrearReservaDetalleViewModel(
        ReservaBackofficeDetalleDto reserva,
        string? errorCambioEstado = null,
        string? mensajeExito = null)
    {
        return new BackofficeReservaDetalleViewModel
        {
            Reserva =
                reserva,

            TransicionesDisponibles =
                GetTransicionesDisponibles(
                    reserva.Estado),

            ErrorCambioEstado =
                errorCambioEstado,

            MensajeExito =
                mensajeExito
        };
    }

    private static IReadOnlyList<string> GetTransicionesDisponibles(
        string? estadoActual)
    {
        return estadoActual?
            .Trim()
            .ToUpperInvariant() switch
        {
            "PENDIENTE" =>
                new[]
                {
                    "CONFIRMADA",
                    "CANCELADA",
                    "EXPIRADA"
                },

            "CONFIRMADA" =>
                new[]
                {
                    "EN_PROCESO",
                    "CANCELADA"
                },

            "EN_PROCESO" =>
                new[]
                {
                    "COMPLETADA"
                },

            _ =>
                Array.Empty<string>()
        };
    }

    private async Task<IActionResult> HandleBackendStatusAsync(
        int statusCode)
    {
        if (
            statusCode ==
            StatusCodes.Status401Unauthorized
        )
        {
            var returnUrl =
                $"{Request.PathBase}" +
                $"{Request.Path}" +
                $"{Request.QueryString}";

            HttpContext.Session.Clear();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults
                    .AuthenticationScheme);

            return RedirectToAction(
                "Login",
                "Account",
                new
                {
                    returnUrl
                });
        }

        if (
            statusCode ==
            StatusCodes.Status403Forbidden
        )
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (
            statusCode ==
            StatusCodes.Status404NotFound
        )
        {
            return NotFound();
        }

        return StatusCode(
            StatusCodes.Status502BadGateway);
    }
}
