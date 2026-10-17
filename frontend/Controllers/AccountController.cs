using System.Security.Claims;
using BembosEvents.Web.Models.Auth;
using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Controllers;

[Route("cuenta")]
public sealed class AccountController : Controller
{
    private readonly IBackendApiClient _backendApiClient;
    private readonly ILogger<AccountController> _logger;

    public AccountController(
        IBackendApiClient backendApiClient,
        ILogger<AccountController> logger)
    {
        _backendApiClient = backendApiClient;
        _logger = logger;
    }

    [AllowAnonymous]
    [HttpGet("iniciar-sesion")]
    public IActionResult Login(
        string? returnUrl = null)
    {
        if (User.Identity?.IsAuthenticated == true)
        {
            return RedirectToAction(
                "Index",
                "Home");
        }

        return View(
            new LoginViewModel
            {
                ReturnUrl = returnUrl
            });
    }

    [AllowAnonymous]
    [HttpPost("iniciar-sesion")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(
        LoginViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var normalizedEmail =
            model.Correo
                .Trim()
                .ToLowerInvariant();

        var loginResult =
            await _backendApiClient.LoginAsync(
                new LoginRequestDto
                {
                    Correo = normalizedEmail,
                    Password = model.Password
                },
                cancellationToken);

        if (
            !loginResult.Success ||
            loginResult.Auth is null
        )
        {
            ModelState.AddModelError(
                string.Empty,
                loginResult.ErrorMessage ??
                "No fue posible iniciar sesion.");

            return View(model);
        }

        var auth =
            loginResult.Auth;

        var expiresAtUtc =
            DateTimeOffset.UtcNow.AddMilliseconds(
                auth.ExpiresInMs);

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendJwt,
            auth.Token);

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendJwtExpiresAtUtc,
            expiresAtUtc.ToString("O"));

        var claims =
            new[]
            {
                new Claim(
                    ClaimTypes.Name,
                    normalizedEmail),

                new Claim(
                    ClaimTypes.Email,
                    normalizedEmail)
            };

        var identity =
            new ClaimsIdentity(
                claims,
                CookieAuthenticationDefaults.AuthenticationScheme);

        var principal =
            new ClaimsPrincipal(
                identity);

        var authenticationProperties =
            new AuthenticationProperties
            {
                IsPersistent = false,
                AllowRefresh = false,
                ExpiresUtc = expiresAtUtc
            };

        await HttpContext.SignInAsync(
            CookieAuthenticationDefaults.AuthenticationScheme,
            principal,
            authenticationProperties);

        _logger.LogInformation(
            "Usuario autenticado correctamente en el portal MVC.");

        if (
            !string.IsNullOrWhiteSpace(model.ReturnUrl) &&
            Url.IsLocalUrl(model.ReturnUrl)
        )
        {
            return LocalRedirect(
                model.ReturnUrl);
        }

        return RedirectToAction(
            "Index",
            "Home");
    }

    [Authorize]
    [HttpPost("cerrar-sesion")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout()
    {
        HttpContext.Session.Clear();

        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults.AuthenticationScheme);

        return RedirectToAction(
            "Index",
            "Home");
    }

    [AllowAnonymous]
    [HttpGet("registrarse")]
    public IActionResult Register()
    {
        if (User.Identity?.IsAuthenticated == true)
        {
            return RedirectToAction(
                "Index",
                "Home");
        }

        return View(
            new RegisterViewModel());
    }

    [AllowAnonymous]
    [HttpPost("registrarse")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Register(
        RegisterViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var normalizedEmail =
            model.Correo
                .Trim()
                .ToLowerInvariant();

        var result =
            await _backendApiClient.RegisterAsync(
                new RegistroRequestDto
                {
                    Nombres =
                        model.Nombres.Trim(),

                    Apellidos =
                        model.Apellidos.Trim(),

                    Correo =
                        normalizedEmail,

                    Telefono =
                        string.IsNullOrWhiteSpace(
                            model.Telefono)
                            ? null
                            : model.Telefono.Trim(),

                    Password =
                        model.Password
                },
                cancellationToken);

        if (!result.Success)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message ??
                "No fue posible registrar la cuenta.");

            return View(model);
        }

        TempData["RegistrationSuccess"] =
            result.Message ??
            "Usuario registrado correctamente";

        return RedirectToAction(
            nameof(Login));
    }
    [AllowAnonymous]
    [HttpGet("acceso-denegado")]
    public IActionResult AccessDenied()
    {
        return View();
    }
}
