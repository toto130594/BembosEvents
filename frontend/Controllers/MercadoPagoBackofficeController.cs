using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[BackofficeAuthorize]
[Route("backoffice/mercado-pago")]
public sealed class MercadoPagoBackofficeController :
    Controller
{
    private readonly IMercadoPagoApiClient _mercadoPagoApiClient;

    public MercadoPagoBackofficeController(
        IMercadoPagoApiClient mercadoPagoApiClient)
    {
        _mercadoPagoApiClient =
            mercadoPagoApiClient;
    }

    [HttpGet("")]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        try
        {
            var result =
                await _mercadoPagoApiClient
                    .GetBackofficePaymentsAsync(
                        cancellationToken);

            if (!result.Success)
            {
                TempData["MercadoPagoMessage"] =
                    ActionErrorMessage(
                        result.StatusCode);

                return View(
                    new MercadoPagoBackofficeViewModel
                    {
                        BackendAvailable =
                            false
                    });
            }

            return View(
                new MercadoPagoBackofficeViewModel
                {
                    Pagos =
                        result.Data ??
                        Array.Empty<
                            BembosEvents.Web.Models.MercadoPago.MercadoPagoPaymentDto>()
                });
        }
        catch (HttpRequestException)
        {
            TempData["MercadoPagoMessage"] =
                "No fue posible comunicarse con Spring Boot.";

            return View(
                new MercadoPagoBackofficeViewModel
                {
                    BackendAvailable =
                        false
                });
        }
    }

    [HttpPost("reembolso/{idPago:int}")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Refund(
        int idPago,
        CancellationToken cancellationToken)
    {
        try
        {
            var result =
                await _mercadoPagoApiClient
                    .RefundAsync(
                        idPago,
                        cancellationToken);

            TempData["MercadoPagoMessage"] =
                result.Success
                    ? "Reembolso total procesado correctamente."
                    : ActionErrorMessage(
                        result.StatusCode);
        }
        catch (HttpRequestException)
        {
            TempData["MercadoPagoMessage"] =
                "No fue posible comunicarse con Spring Boot.";
        }

        return RedirectToAction(
            nameof(Index));
    }

    [HttpPost("reconciliar/{idPago:int}")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Reconcile(
        int idPago,
        CancellationToken cancellationToken)
    {
        try
        {
            var result =
                await _mercadoPagoApiClient
                    .ReconcileAsync(
                        idPago,
                        cancellationToken);

            TempData["MercadoPagoMessage"] =
                result.Success &&
                result.Data is not null
                    ? $"Reconciliacion: {result.Data.Resultado}."
                    : ActionErrorMessage(
                        result.StatusCode);
        }
        catch (HttpRequestException)
        {
            TempData["MercadoPagoMessage"] =
                "No fue posible comunicarse con Spring Boot.";
        }

        return RedirectToAction(
            nameof(Index));
    }

    [HttpPost("reconciliar-lote")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ReconcileBatch(
        CancellationToken cancellationToken)
    {
        try
        {
            var result =
                await _mercadoPagoApiClient
                    .ReconcileBatchAsync(
                        cancellationToken);

            if (
                result.Success &&
                result.Data is not null
            )
            {
                TempData["MercadoPagoMessage"] =
                    $"Reconciliacion finalizada. Total: {result.Data.Total}, " +
                    $"actualizados: {result.Data.Actualizados}, " +
                    $"conflictos: {result.Data.Conflictos}, " +
                    $"errores: {result.Data.Errores}.";
            }
            else
            {
                TempData["MercadoPagoMessage"] =
                    ActionErrorMessage(
                        result.StatusCode);
            }
        }
        catch (HttpRequestException)
        {
            TempData["MercadoPagoMessage"] =
                "No fue posible comunicarse con Spring Boot.";
        }

        return RedirectToAction(
            nameof(Index));
    }

    private static string ActionErrorMessage(
        int statusCode)
    {
        return statusCode switch
        {
            401 =>
                "La sesion backend ya no es valida.",

            403 =>
                "Spring Boot rechazo la operacion. Los reembolsos y la reconciliacion requieren ADMINISTRADOR.",

            404 =>
                "El pago solicitado no existe.",

            409 =>
                "La operacion no es valida para el estado actual del pago.",

            _ =>
                "Spring Boot no pudo completar la operacion de Mercado Pago."
        };
    }
}