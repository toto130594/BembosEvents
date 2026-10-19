using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;

namespace BembosEvents.Web.Controllers;

[Authorize]
public sealed class PagoController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public PagoController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("/reservar/mis-reservas/{idReserva:int}/pagos")]
    public async Task<IActionResult> PagosReserva(
        [FromRoute] int idReserva,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        var reservaResult =
            await _backendApiClient.GetReservaPropiaAsync(
                idReserva,
                cancellationToken);

        if (reservaResult.StatusCode == 401)
        {
            return RedirectToLogin(
                $"/reservar/mis-reservas/{idReserva}/pagos");
        }

        if (reservaResult.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (reservaResult.StatusCode == 404)
        {
            return NotFound();
        }

        if (
            !reservaResult.IsSuccess ||
            reservaResult.Data is null
        )
        {
            TempData["PaymentError"] =
                "No fue posible consultar la reserva.";

            return Redirect(
                "/reservar/mis-reservas");
        }

        var pagosResult =
            await _backendApiClient.GetPagosReservaAsync(
                idReserva,
                cancellationToken);

        if (pagosResult.StatusCode == 401)
        {
            return RedirectToLogin(
                $"/reservar/mis-reservas/{idReserva}/pagos");
        }

        if (pagosResult.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (pagosResult.StatusCode == 404)
        {
            return NotFound();
        }

        if (!pagosResult.IsSuccess)
        {
            TempData["PaymentError"] =
                "No fue posible consultar los pagos.";

            return Redirect(
                $"/reservar/mis-reservas/{idReserva}");
        }

        return View(
            "PagosReserva",
            new PagosReservaViewModel
            {
                Reserva =
                    reservaResult.Data,

                Pagos =
                    pagosResult.Data
                    ?? Array.Empty<PagoDto>()
            });
    }

    [HttpGet("/reservar/mis-reservas/{idReserva:int}/pagos/nuevo")]
    public async Task<IActionResult> Nuevo(
        [FromRoute] int idReserva,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        var reservaResult =
            await _backendApiClient.GetReservaPropiaAsync(
                idReserva,
                cancellationToken);

        if (reservaResult.StatusCode == 401)
        {
            return RedirectToLogin(
                $"/reservar/mis-reservas/{idReserva}/pagos/nuevo");
        }

        if (reservaResult.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (reservaResult.StatusCode == 404)
        {
            return NotFound();
        }

        if (
            !reservaResult.IsSuccess ||
            reservaResult.Data is null
        )
        {
            TempData["PaymentError"] =
                "No fue posible cargar la reserva.";

            return Redirect(
                $"/reservar/mis-reservas/{idReserva}/pagos");
        }

        return View(
            "Nuevo",
            CrearNuevoPagoViewModel(
                reservaResult.Data));
    }

    [HttpPost("/reservar/mis-reservas/{idReserva:int}/pagos")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Crear(
        [FromRoute] int idReserva,
        NuevoPagoViewModel model,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        model.IdReserva =
            idReserva;

        ModelState.Remove(
            nameof(model.IdReserva));

        if (!ModelState.IsValid)
        {
            var invalidContextResult =
                await _backendApiClient.GetReservaPropiaAsync(
                    idReserva,
                    cancellationToken);

            var contextFailure =
                HandleReservaContextFailure(
                    invalidContextResult,
                    $"/reservar/mis-reservas/{idReserva}/pagos/nuevo");

            if (contextFailure is not null)
            {
                return contextFailure;
            }

            AplicarContextoReserva(
                model,
                invalidContextResult.Data!);

            return View(
                "Nuevo",
                model);
        }

        var request =
            new CrearPagoRequestDto
            {
                Monto =
                    model.Monto!.Value,

                MetodoPago =
                    model.MetodoPago.Trim()
            };

        var result =
            await _backendApiClient.CrearPagoAsync(
                idReserva,
                request,
                cancellationToken);

        if (result.StatusCode == 401)
        {
            return RedirectToLogin(
                $"/reservar/mis-reservas/{idReserva}/pagos/nuevo");
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (result.StatusCode == 404)
        {
            return NotFound();
        }

        if (
            result.StatusCode == 400 ||
            result.StatusCode == 409
        )
        {
            ModelState.AddModelError(
                string.Empty,
                result.StatusCode == 409
                    ? "El pago no puede registrarse en el estado actual de la reserva."
                    : "Spring Boot rechazo los datos del pago.");

            var contextResult =
                await _backendApiClient.GetReservaPropiaAsync(
                    idReserva,
                    cancellationToken);

            var contextFailure =
                HandleReservaContextFailure(
                    contextResult,
                    $"/reservar/mis-reservas/{idReserva}/pagos/nuevo");

            if (contextFailure is not null)
            {
                return contextFailure;
            }

            AplicarContextoReserva(
                model,
                contextResult.Data!);

            return View(
                "Nuevo",
                model);
        }

        if (
            !result.IsSuccess ||
            result.Data is null
        )
        {
            ModelState.AddModelError(
                string.Empty,
                "No fue posible registrar el pago.");

            var contextResult =
                await _backendApiClient.GetReservaPropiaAsync(
                    idReserva,
                    cancellationToken);

            var contextFailure =
                HandleReservaContextFailure(
                    contextResult,
                    $"/reservar/mis-reservas/{idReserva}/pagos/nuevo");

            if (contextFailure is not null)
            {
                return contextFailure;
            }

            AplicarContextoReserva(
                model,
                contextResult.Data!);

            return View(
                "Nuevo",
                model);
        }

        TempData["PaymentSuccess"] =
            "Pago registrado correctamente.";

        return RedirectToAction(
            nameof(Detalle),
            new
            {
                idPago =
                    result.Data.IdPago
            });
    }

    [HttpGet("/pagos/{idPago:int}")]
    public async Task<IActionResult> Detalle(
        [FromRoute] int idPago,
        CancellationToken cancellationToken)
    {
        if (idPago <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient.GetPagoPropioAsync(
                idPago,
                cancellationToken);

        if (result.StatusCode == 401)
        {
            return RedirectToLogin(
                $"/pagos/{idPago}");
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (result.StatusCode == 404)
        {
            return NotFound();
        }

        if (
            !result.IsSuccess ||
            result.Data is null
        )
        {
            return StatusCode(502);
        }

        return View(
            "Detalle",
            new PagoDetalleViewModel
            {
                Pago =
                    result.Data
            });
    }

    private IActionResult? HandleReservaContextFailure(
        BackendApiResult<ReservaDetalleDto> result,
        string returnUrl)
    {
        if (result.StatusCode == 401)
        {
            return RedirectToLogin(
                returnUrl);
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (result.StatusCode == 404)
        {
            return NotFound();
        }

        if (
            !result.IsSuccess ||
            result.Data is null
        )
        {
            return StatusCode(502);
        }

        return null;
    }

    private static NuevoPagoViewModel CrearNuevoPagoViewModel(
        ReservaDetalleDto reserva)
    {
        var model =
            new NuevoPagoViewModel();

        AplicarContextoReserva(
            model,
            reserva);

        return model;
    }

    private static void AplicarContextoReserva(
        NuevoPagoViewModel model,
        ReservaDetalleDto reserva)
    {
        model.IdReserva =
            reserva.IdReserva;

        model.CodigoReserva =
            reserva.CodigoReserva;

        model.EstadoReserva =
            reserva.Estado;

        model.MontoTotal =
            reserva.MontoTotal;

        model.MontoDeposito =
            reserva.MontoDeposito;
    }

    private IActionResult RedirectToLogin(
        string returnUrl)
    {
        ClearBackendAuthenticationSession();

        return Redirect(
            "/cuenta/iniciar-sesion?ReturnUrl=" +
            Uri.EscapeDataString(returnUrl));
    }

    private void ClearBackendAuthenticationSession()
    {
        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendJwt);

        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendJwtExpiresAtUtc);
    }
}
