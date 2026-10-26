using BembosEvents.Web.Models.MercadoPago;
using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Models.MercadoPago;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[Authorize]
[Route("pagos/mercado-pago")]
public sealed class MercadoPagoController :
    Controller
{
    private readonly IMercadoPagoApiClient _mercadoPagoApiClient;
    private readonly IBackendApiClient _backendApiClient;

    public MercadoPagoController(
        IMercadoPagoApiClient mercadoPagoApiClient,
        IBackendApiClient backendApiClient)
    {
        _mercadoPagoApiClient =
            mercadoPagoApiClient;

        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public IActionResult Index(
        int? idReserva = null,
        decimal? monto = null)
    {
        return View(
            new MercadoPagoCheckoutViewModel
            {
                IdReserva =
                    idReserva.GetValueOrDefault(),

                Monto =
                    monto.GetValueOrDefault()
            });
    }

    [HttpPost("checkout")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Checkout(
        MercadoPagoCheckoutViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(
                "Index",
                model);
        }

        MercadoPagoApiResult<
            BembosEvents.Web.Models.MercadoPago.MercadoPagoCheckoutDto>
            result;

        try
        {
            result =
                await _mercadoPagoApiClient
                    .StartCheckoutAsync(
                        model.IdReserva,
                        model.Monto,
                        cancellationToken);
        }
        catch (HttpRequestException)
        {
            ModelState.AddModelError(
                string.Empty,
                "No fue posible procesar el pago en este momento. Intentalo nuevamente.");

            return View(
                "Index",
                model);
        }

        if (
            !result.Success ||
            result.Data is null
        )
        {
            ModelState.AddModelError(
                string.Empty,
                PaymentErrorMessage(
                    result.StatusCode));

            return View(
                "Index",
                model);
        }

        if (!IsTrustedCheckoutUrl(
            result.Data.CheckoutUrl))
        {
            ModelState.AddModelError(
                string.Empty,
                "No fue posible abrir Mercado Pago de forma segura. Intentalo nuevamente.");

            return View(
                "Index",
                model);
        }

        return Redirect(
            result.Data.CheckoutUrl);
    }

    [HttpGet("resultado")]
    public async Task<IActionResult> Resultado(
        string? pago = null,
        string? resultado = null,
        int intento = 0,
        CancellationToken cancellationToken = default)
    {
        if (!TryParsePagoId(
            pago,
            out var idPago))
        {
            return View(
                "Resultado",
                new MercadoPagoResultadoViewModel
                {
                    ResultadoRetorno =
                        resultado ?? string.Empty,

                    Mensaje =
                        "No se pudo identificar el pago retornado por Mercado Pago."
                });
        }

        try
        {
            var pagoResult =
                await _backendApiClient
                    .GetPagoPropioAsync(
                        idPago,
                        cancellationToken);

            if (
                !IsHttpSuccess(
                    pagoResult.StatusCode) ||
                pagoResult.Data is null
            )
            {
                return View(
                    "Resultado",
                    new MercadoPagoResultadoViewModel
                    {
                        ResultadoRetorno =
                            resultado ?? string.Empty,

                        Intento =
                            intento,

                        Mensaje =
                            "No fue posible consultar el estado actual del pago."
                    });
            }

            var pagoActual =
                pagoResult.Data;

            var reservaResult =
                await _backendApiClient
                    .GetReservaPropiaAsync(
                        pagoActual.IdReserva,
                        cancellationToken);

            var pagosResult =
                await _backendApiClient
                    .GetPagosReservaAsync(
                        pagoActual.IdReserva,
                        cancellationToken);

            if (
                !IsHttpSuccess(
                    reservaResult.StatusCode) ||
                reservaResult.Data is null ||
                !IsHttpSuccess(
                    pagosResult.StatusCode)
            )
            {
                return View(
                    "Resultado",
                    new MercadoPagoResultadoViewModel
                    {
                        ResultadoRetorno =
                            resultado ?? string.Empty,

                        Pago =
                            pagoActual,

                        Intento =
                            intento,

                        Mensaje =
                            "El pago fue localizado, pero no se pudo cargar el resumen completo de la reserva."
                    });
            }

            var pagos =
                pagosResult.Data ??
                Array.Empty<PagoDto>();

            var totalPagado =
                pagos
                    .Where(
                        item =>
                            string.Equals(
                                item.EstadoPago,
                                "APROBADO",
                                StringComparison.OrdinalIgnoreCase))
                    .Sum(
                        item =>
                            item.Monto);

            var saldoPendiente =
                Math.Max(
                    0m,
                    reservaResult.Data.MontoTotal -
                    totalPagado);

            return View(
                "Resultado",
                new MercadoPagoResultadoViewModel
                {
                    ResultadoRetorno =
                        resultado ?? string.Empty,

                    Pago =
                        pagoActual,

                    Reserva =
                        reservaResult.Data,

                    Pagos =
                        pagos
                            .OrderByDescending(
                                item =>
                                    item.FechaCreacion)
                            .ToArray(),

                    TotalPagado =
                        totalPagado,

                    SaldoPendiente =
                        saldoPendiente,

                    Intento =
                        intento
                });
        }
        catch (HttpRequestException)
        {
            return View(
                "Resultado",
                new MercadoPagoResultadoViewModel
                {
                    ResultadoRetorno =
                        resultado ?? string.Empty,

                    Intento =
                        intento,

                    Mensaje =
                        "No fue posible comunicarse con Spring Boot para verificar el pago."
                });
        }
    }

    private static bool TryParsePagoId(
        string? value,
        out int idPago)
    {
        idPago = 0;

        if (string.IsNullOrWhiteSpace(
            value))
        {
            return false;
        }

        var normalized =
            value.Trim();

        if (normalized.StartsWith(
            "PAGO-",
            StringComparison.OrdinalIgnoreCase))
        {
            normalized =
                normalized[5..];
        }

        return
            int.TryParse(
                normalized,
                out idPago) &&
            idPago > 0;
    }

    private static bool IsHttpSuccess(
        int statusCode)
    {
        return
            statusCode >= 200 &&
            statusCode < 300;
    }
    private static bool IsTrustedCheckoutUrl(
        string? value)
    {
        if (
            string.IsNullOrWhiteSpace(value) ||
            !Uri.TryCreate(
                value,
                UriKind.Absolute,
                out var uri) ||
            !string.Equals(
                uri.Scheme,
                Uri.UriSchemeHttps,
                StringComparison.OrdinalIgnoreCase)
        )
        {
            return false;
        }

        var host =
            uri.Host.ToLowerInvariant();

        return
            host == "mercadopago.com" ||
            host.EndsWith(
                ".mercadopago.com",
                StringComparison.Ordinal) ||
            host == "mercadopago.com.pe" ||
            host.EndsWith(
                ".mercadopago.com.pe",
                StringComparison.Ordinal);
    }

    private static string PaymentErrorMessage(
        int statusCode)
    {
        return statusCode switch
        {
            400 =>
                "Revisa los datos del pago e intentalo nuevamente.",

            401 =>
                "Tu sesion ha vencido. Inicia sesion nuevamente.",

            403 =>
                "No tienes permisos para iniciar este pago.",

            404 =>
                "La reserva no existe o no te pertenece.",

            409 =>
                "El pago no pudo iniciarse por el estado actual del recurso.",

            _ =>
                "No fue posible iniciar el checkout de Mercado Pago."
        };
    }
}