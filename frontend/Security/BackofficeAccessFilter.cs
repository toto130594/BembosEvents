using BembosEvents.Web.Services;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace BembosEvents.Web.Security;

public sealed class BackofficeAccessFilter :
    IAsyncAuthorizationFilter
{
    private readonly IBackendApiClient _backendApiClient;

    public BackofficeAccessFilter(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    public async Task OnAuthorizationAsync(
        AuthorizationFilterContext context)
    {
        var httpContext =
            context.HttpContext;

        if (
            httpContext.User.Identity?.IsAuthenticated !=
            true
        )
        {
            context.Result =
                new ChallengeResult();

            return;
        }

        int statusCode;

        try
        {
            statusCode =
                await _backendApiClient
                    .GetBackofficeAccessStatusAsync(
                        httpContext.RequestAborted);
        }
        catch (HttpRequestException)
        {
            context.Result =
                new StatusCodeResult(
                    StatusCodes.Status503ServiceUnavailable);

            return;
        }

        if (statusCode == StatusCodes.Status200OK)
        {
            return;
        }

        if (statusCode == StatusCodes.Status401Unauthorized)
        {
            var returnUrl =
                $"{httpContext.Request.PathBase}" +
                $"{httpContext.Request.Path}" +
                $"{httpContext.Request.QueryString}";

            httpContext.Session.Clear();

            await httpContext.SignOutAsync(
                CookieAuthenticationDefaults
                    .AuthenticationScheme);

            context.Result =
                new RedirectToActionResult(
                    "Login",
                    "Account",
                    new
                    {
                        returnUrl
                    });

            return;
        }

        if (statusCode == StatusCodes.Status403Forbidden)
        {
            context.Result =
                new RedirectToActionResult(
                    "AccessDenied",
                    "Account",
                    null);

            return;
        }

        if (statusCode == StatusCodes.Status404NotFound)
        {
            context.Result =
                new NotFoundResult();

            return;
        }

        context.Result =
            new StatusCodeResult(
                StatusCodes.Status502BadGateway);
    }
}
