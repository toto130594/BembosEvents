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

    public MercadoPagoController(
        IMercadoPagoApiClient mercadoPagoApiClient)
    {
        _mercadoPagoApiClient =
            mercadoPagoApiClient;
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
                "No fue posible comunicarse con el backend de pagos.");

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
                "El backend devolvio una URL de checkout no permitida.");

            return View(
                "Index",
                model);
        }

        return Redirect(
            result.Data.CheckoutUrl);
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
                "Spring Boot rechazo los datos del pago.",

            401 =>
                "La sesion backend ya no es valida.",

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