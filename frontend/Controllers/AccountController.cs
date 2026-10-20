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

        if (
            TempData["MfaError"] is string mfaError &&
            !string.IsNullOrWhiteSpace(mfaError)
        )
        {
            ModelState.AddModelError(
                string.Empty,
                mfaError);
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
                    Correo =
                        normalizedEmail,

                    Password =
                        model.Password
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

        if (auth.MfaRequired)
        {
            if (
                string.IsNullOrWhiteSpace(
                    auth.MfaChallengeToken) ||
                auth.MfaChallengeExpiresInMs <= 0 ||
                !string.IsNullOrWhiteSpace(
                    auth.Token) ||
                !string.IsNullOrWhiteSpace(
                    auth.RefreshToken)
            )
            {
                ModelState.AddModelError(
                    string.Empty,
                    "La respuesta MFA no es valida.");

                return View(model);
            }

            ClearBackendAuthentication();
            ClearPendingMfa();

            var challengeExpiresAtUtc =
                DateTimeOffset.UtcNow
                    .AddMilliseconds(
                        auth.MfaChallengeExpiresInMs);

            HttpContext.Session.SetString(
                FrontendSessionKeys.PendingMfaChallengeToken,
                auth.MfaChallengeToken);

            HttpContext.Session.SetString(
                FrontendSessionKeys.PendingMfaChallengeExpiresAtUtc,
                challengeExpiresAtUtc.ToString("O"));

            HttpContext.Session.SetString(
                FrontendSessionKeys.PendingMfaEmail,
                normalizedEmail);

            if (
                !string.IsNullOrWhiteSpace(
                    model.ReturnUrl) &&
                Url.IsLocalUrl(
                    model.ReturnUrl)
            )
            {
                HttpContext.Session.SetString(
                    FrontendSessionKeys.PendingMfaReturnUrl,
                    model.ReturnUrl);
            }

            _logger.LogInformation(
                "Segundo factor MFA requerido para completar el login MVC.");

            return RedirectToAction(
                nameof(Mfa));
        }

        if (!HasCompleteSession(auth))
        {
            ModelState.AddModelError(
                string.Empty,
                "La respuesta de autenticacion no contiene una sesion valida.");

            return View(model);
        }

        return await CompleteAuthenticationAsync(
            normalizedEmail,
            model.ReturnUrl,
            auth);
    }

    [AllowAnonymous]
    [HttpGet("mfa")]
    public IActionResult Mfa()
    {
        if (User.Identity?.IsAuthenticated == true)
        {
            return RedirectToAction(
                "Index",
                "Home");
        }

        if (
            !TryGetPendingMfaState(
                out _,
                out _,
                out _)
        )
        {
            ClearPendingMfa();

            TempData["MfaError"] =
                "El desafio de seguridad expiro. Inicia sesion nuevamente.";

            return RedirectToAction(
                nameof(Login));
        }

        return View(
            new MfaChallengeViewModel());
    }

    [AllowAnonymous]
    [HttpPost("mfa/verificar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> MfaVerify(
        MfaChallengeViewModel model,
        CancellationToken cancellationToken)
    {
        if (
            !TryGetPendingMfaState(
                out var challengeToken,
                out var email,
                out var returnUrl)
        )
        {
            ClearPendingMfa();

            TempData["MfaError"] =
                "El desafio de seguridad expiro. Inicia sesion nuevamente.";

            return RedirectToAction(
                nameof(Login));
        }

        if (string.IsNullOrWhiteSpace(model.Code))
        {
            ModelState.AddModelError(
                nameof(model.Code),
                "Ingresa el codigo de autenticacion.");

            return View(
                "Mfa",
                model);
        }

        if (!ModelState.IsValid)
        {
            return View(
                "Mfa",
                model);
        }

        var result =
            await _backendApiClient.VerifyMfaTotpAsync(
                challengeToken,
                model.Code.Trim(),
                cancellationToken);

        if (
            !result.Success ||
            result.Auth is null
        )
        {
            ModelState.AddModelError(
                string.Empty,
                result.ErrorMessage ??
                "No fue posible verificar el codigo.");

            return View(
                "Mfa",
                model);
        }

        if (!HasCompleteSession(result.Auth))
        {
            ClearPendingMfa();

            TempData["MfaError"] =
                "La respuesta de autenticacion no contiene una sesion valida.";

            return RedirectToAction(
                nameof(Login));
        }

        return await CompleteAuthenticationAsync(
            email,
            returnUrl,
            result.Auth);
    }

    [AllowAnonymous]
    [HttpPost("mfa/recuperacion")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> MfaRecovery(
        MfaChallengeViewModel model,
        CancellationToken cancellationToken)
    {
        if (
            !TryGetPendingMfaState(
                out var challengeToken,
                out var email,
                out var returnUrl)
        )
        {
            ClearPendingMfa();

            TempData["MfaError"] =
                "El desafio de seguridad expiro. Inicia sesion nuevamente.";

            return RedirectToAction(
                nameof(Login));
        }

        ModelState.Remove(
            nameof(model.Code));

        if (
            string.IsNullOrWhiteSpace(
                model.RecoveryCode)
        )
        {
            ModelState.AddModelError(
                nameof(model.RecoveryCode),
                "Ingresa un codigo de recuperacion.");

            return View(
                "Mfa",
                model);
        }

        if (!ModelState.IsValid)
        {
            return View(
                "Mfa",
                model);
        }

        var result =
            await _backendApiClient.VerifyMfaRecoveryAsync(
                challengeToken,
                model.RecoveryCode.Trim(),
                cancellationToken);

        if (
            !result.Success ||
            result.Auth is null
        )
        {
            ModelState.AddModelError(
                string.Empty,
                result.ErrorMessage ??
                "No fue posible verificar el codigo de recuperacion.");

            return View(
                "Mfa",
                model);
        }

        if (!HasCompleteSession(result.Auth))
        {
            ClearPendingMfa();

            TempData["MfaError"] =
                "La respuesta de autenticacion no contiene una sesion valida.";

            return RedirectToAction(
                nameof(Login));
        }

        return await CompleteAuthenticationAsync(
            email,
            returnUrl,
            result.Auth);
    }

    private async Task<IActionResult> CompleteAuthenticationAsync(
        string normalizedEmail,
        string? returnUrl,
        AuthResponseDto auth)
    {
        if (!HasCompleteSession(auth))
        {
            ClearBackendAuthentication();
            ClearPendingMfa();

            TempData["MfaError"] =
                "No fue posible crear una sesion valida.";

            return RedirectToAction(
                nameof(Login));
        }

        var accessExpiresAtUtc =
            DateTimeOffset.UtcNow
                .AddMilliseconds(
                    auth.ExpiresInMs);

        var refreshExpiresAtUtc =
            DateTimeOffset.UtcNow
                .AddMilliseconds(
                    auth.RefreshTokenExpiresInMs);

        ClearPendingMfa();

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendJwt,
            auth.Token!);

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendJwtExpiresAtUtc,
            accessExpiresAtUtc.ToString("O"));

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendRefreshToken,
            auth.RefreshToken!);

        HttpContext.Session.SetString(
            FrontendSessionKeys.BackendRefreshTokenExpiresAtUtc,
            refreshExpiresAtUtc.ToString("O"));

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
                ExpiresUtc = refreshExpiresAtUtc
            };

        await HttpContext.SignInAsync(
            CookieAuthenticationDefaults.AuthenticationScheme,
            principal,
            authenticationProperties);

        _logger.LogInformation(
            "Usuario autenticado correctamente en el portal MVC.");

        if (
            !string.IsNullOrWhiteSpace(
                returnUrl) &&
            Url.IsLocalUrl(
                returnUrl)
        )
        {
            return LocalRedirect(
                returnUrl);
        }

        return RedirectToAction(
            "Index",
            "Home");
    }

    private static bool HasCompleteSession(
        AuthResponseDto auth)
    {
        return
            !auth.MfaRequired &&
            !string.IsNullOrWhiteSpace(
                auth.Token) &&
            auth.ExpiresInMs > 0 &&
            !string.IsNullOrWhiteSpace(
                auth.RefreshToken) &&
            auth.RefreshTokenExpiresInMs > 0;
    }

    private bool TryGetPendingMfaState(
        out string challengeToken,
        out string email,
        out string? returnUrl)
    {
        challengeToken =
            HttpContext.Session.GetString(
                FrontendSessionKeys.PendingMfaChallengeToken) ??
            string.Empty;

        email =
            HttpContext.Session.GetString(
                FrontendSessionKeys.PendingMfaEmail) ??
            string.Empty;

        returnUrl =
            HttpContext.Session.GetString(
                FrontendSessionKeys.PendingMfaReturnUrl);

        var expirationValue =
            HttpContext.Session.GetString(
                FrontendSessionKeys.PendingMfaChallengeExpiresAtUtc);

        if (
            string.IsNullOrWhiteSpace(
                challengeToken) ||
            string.IsNullOrWhiteSpace(
                email) ||
            string.IsNullOrWhiteSpace(
                expirationValue) ||
            !DateTimeOffset.TryParse(
                expirationValue,
                out var expiresAtUtc) ||
            expiresAtUtc <=
                DateTimeOffset.UtcNow
        )
        {
            challengeToken =
                string.Empty;

            email =
                string.Empty;

            returnUrl =
                null;

            return false;
        }

        return true;
    }

    private void ClearPendingMfa()
    {
        HttpContext.Session.Remove(
            FrontendSessionKeys.PendingMfaChallengeToken);

        HttpContext.Session.Remove(
            FrontendSessionKeys.PendingMfaChallengeExpiresAtUtc);

        HttpContext.Session.Remove(
            FrontendSessionKeys.PendingMfaEmail);

        HttpContext.Session.Remove(
            FrontendSessionKeys.PendingMfaReturnUrl);
    }

    private void ClearBackendAuthentication()
    {
        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendJwt);

        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendJwtExpiresAtUtc);

        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendRefreshToken);

        HttpContext.Session.Remove(
            FrontendSessionKeys.BackendRefreshTokenExpiresAtUtc);
    }
    [AllowAnonymous]
    [HttpGet("recuperar-contrasena")]
    public IActionResult ForgotPassword()
    {
        return View(
            new ForgotPasswordViewModel());
    }

    [AllowAnonymous]
    [HttpPost("recuperar-contrasena")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ForgotPassword(
        ForgotPasswordViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var correo =
            model.Correo
                .Trim()
                .ToLowerInvariant();

        var result =
            await _backendApiClient
                .RequestPasswordRecoveryAsync(
                    correo,
                    cancellationToken);

        if (!result.Success)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message ??
                "No fue posible procesar la solicitud.");

            return View(model);
        }

        TempData["PasswordRecoveryRequested"] =
            result.Message ??
            "Si el correo existe, se enviaran instrucciones para restablecer la contrasena.";

        return RedirectToAction(
            nameof(ForgotPassword));
    }

    [AllowAnonymous]
    [HttpGet("restablecer-contrasena")]
    public IActionResult ResetPassword(
        string? token = null)
    {
        return View(
            new ResetPasswordViewModel
            {
                Token =
                    token?.Trim() ??
                    string.Empty
            });
    }

    [AllowAnonymous]
    [HttpPost("restablecer-contrasena")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ResetPassword(
        ResetPasswordViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var result =
            await _backendApiClient.ResetPasswordAsync(
                model.Token.Trim(),
                model.NuevaPassword,
                cancellationToken);

        if (!result.Success)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message ??
                "No fue posible restablecer la contrasena.");

            return View(model);
        }

        ClearPendingMfa();
        ClearBackendAuthentication();

        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults.AuthenticationScheme);

        TempData["PasswordResetSuccess"] =
            result.Message ??
            "Contrasena restablecida correctamente.";

        return RedirectToAction(
            nameof(Login));
    }
    [AllowAnonymous]
    [HttpGet("reenviar-verificacion-correo")]
    public IActionResult ResendEmailVerification()
    {
        return View(
            new ResendEmailVerificationViewModel());
    }

    [AllowAnonymous]
    [HttpPost("reenviar-verificacion-correo")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ResendEmailVerification(
        ResendEmailVerificationViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var correo =
            model.Correo
                .Trim()
                .ToLowerInvariant();

        var result =
            await _backendApiClient
                .ResendEmailVerificationAsync(
                    correo,
                    cancellationToken);

        if (!result.Success)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message ??
                "No fue posible procesar la solicitud.");

            return View(model);
        }

        TempData["EmailVerificationRequested"] =
            result.Message ??
            "Si el correo requiere verificacion, se enviaran instrucciones.";

        return RedirectToAction(
            nameof(ResendEmailVerification));
    }

    [AllowAnonymous]
    [HttpGet("verificar-correo")]
    public IActionResult VerifyEmail(
        string? token = null)
    {
        return View(
            new VerifyEmailViewModel
            {
                Token =
                    token?.Trim() ??
                    string.Empty
            });
    }

    [AllowAnonymous]
    [HttpPost("verificar-correo")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> VerifyEmail(
        VerifyEmailViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var result =
            await _backendApiClient.VerifyEmailAsync(
                model.Token.Trim(),
                cancellationToken);

        if (!result.Success)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message ??
                "No fue posible verificar el correo.");

            return View(model);
        }

        ClearPendingMfa();

        TempData["EmailVerificationSuccess"] =
            result.Message ??
            "Correo verificado correctamente.";

        return RedirectToAction(
            nameof(Login));
    }
    [Authorize]
    [HttpGet("sesiones")]
    public async Task<IActionResult> Sessions(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient
                .GetSessionsAsync(
                    cancellationToken);

        if (
            !result.Success &&
            result.StatusCode ==
                System.Net.HttpStatusCode.Unauthorized
        )
        {
            ClearPendingMfa();
            ClearBackendAuthentication();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults.AuthenticationScheme);

            TempData["AuthenticationNotice"] =
                "Tu sesion expiro. Inicia sesion nuevamente.";

            return RedirectToAction(
                nameof(Login));
        }

        return View(
            new SessionsViewModel
            {
                Sessions =
                    result.Sessions,

                ErrorMessage =
                    result.Success
                        ? null
                        : result.Message
            });
    }

    [Authorize]
    [HttpPost("sesiones/{idSesionUsuario:long}/revocar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> RevokeSession(
        long idSesionUsuario,
        CancellationToken cancellationToken)
    {
        if (idSesionUsuario < 1)
        {
            TempData["SessionActionError"] =
                "La sesion indicada no es valida.";

            return RedirectToAction(
                nameof(Sessions));
        }

        var result =
            await _backendApiClient
                .RevokeSessionAsync(
                    idSesionUsuario,
                    cancellationToken);

        if (
            !result.Success &&
            result.StatusCode ==
                System.Net.HttpStatusCode.Unauthorized
        )
        {
            ClearPendingMfa();
            ClearBackendAuthentication();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults.AuthenticationScheme);

            TempData["AuthenticationNotice"] =
                "Tu sesion expiro. Inicia sesion nuevamente.";

            return RedirectToAction(
                nameof(Login));
        }

        if (!result.Success)
        {
            TempData["SessionActionError"] =
                result.Message ??
                "No fue posible cerrar la sesion.";

            return RedirectToAction(
                nameof(Sessions));
        }

        TempData["SessionActionSuccess"] =
            result.Message ??
            "Sesion cerrada correctamente.";

        return RedirectToAction(
            nameof(Sessions));
    }

    [Authorize]
    [HttpPost("sesiones/cerrar-todas")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> LogoutAllSessions(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient
                .LogoutAllSessionsAsync(
                    cancellationToken);

        if (!result.Success)
        {
            if (
                result.StatusCode ==
                    System.Net.HttpStatusCode.Unauthorized
            )
            {
                ClearPendingMfa();
                ClearBackendAuthentication();

                await HttpContext.SignOutAsync(
                    CookieAuthenticationDefaults.AuthenticationScheme);

                TempData["AuthenticationNotice"] =
                    "Tu sesion ya no es valida. Inicia sesion nuevamente.";

                return RedirectToAction(
                    nameof(Login));
            }

            TempData["SessionActionError"] =
                result.Message ??
                "No fue posible cerrar todas las sesiones.";

            return RedirectToAction(
                nameof(Sessions));
        }

        ClearPendingMfa();
        ClearBackendAuthentication();

        HttpContext.Session.Clear();

        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults.AuthenticationScheme);

        TempData["AuthenticationNotice"] =
            "Todas las sesiones fueron cerradas correctamente.";

        return RedirectToAction(
            nameof(Login));
    }
    [Authorize]
    [HttpPost("cerrar-sesion")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout(
        CancellationToken cancellationToken)
    {
        var refreshToken =
            HttpContext.Session.GetString(
                FrontendSessionKeys.BackendRefreshToken);

        try
        {
            if (!string.IsNullOrWhiteSpace(refreshToken))
            {
                var revoked =
                    await _backendApiClient.LogoutAsync(
                        refreshToken,
                        cancellationToken);

                if (!revoked)
                {
                    _logger.LogWarning(
                        "No fue posible confirmar la revocacion backend durante logout MVC.");
                }
            }
        }
        finally
        {
            HttpContext.Session.Clear();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults.AuthenticationScheme);
        }

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
