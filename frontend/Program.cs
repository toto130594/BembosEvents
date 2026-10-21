using BembosEvents.Web.HealthChecks;
using BembosEvents.Web.Security;
using BembosEvents.Web.Services;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllersWithViews();

var configuredDataProtectionPath =
    builder.Configuration[
        "DataProtection:KeysPath"];

var dataProtectionPath =
    string.IsNullOrWhiteSpace(
        configuredDataProtectionPath)
        ? Path.Combine(
            Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData),
            "BembosEvents",
            "DataProtection-Keys")
        : configuredDataProtectionPath;

Directory.CreateDirectory(
    dataProtectionPath);

builder.Services
    .AddDataProtection()
    .PersistKeysToFileSystem(
        new DirectoryInfo(
            dataProtectionPath))
    .SetApplicationName(
        "BembosEvents.Web");

builder.Services
    .AddAuthentication(
        CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.Cookie.Name =
            "BembosEvents.Auth";

        options.Cookie.HttpOnly =
            true;

        options.Cookie.SameSite =
            SameSiteMode.Lax;

        options.Cookie.SecurePolicy =
            CookieSecurePolicy.SameAsRequest;

        options.Cookie.IsEssential =
            true;

        options.LoginPath =
            "/cuenta/iniciar-sesion";

        options.AccessDeniedPath =
            "/cuenta/acceso-denegado";

        options.ExpireTimeSpan =
            TimeSpan.FromHours(1);

        options.SlidingExpiration =
            false;

        options.Events.OnValidatePrincipal =
            context =>
            {
                var token =
                    context.HttpContext
                        .Session
                        .GetString(
                            FrontendSessionKeys.BackendJwt);

                if (string.IsNullOrWhiteSpace(token))
                {
                    context.RejectPrincipal();

                    return context.HttpContext
                        .SignOutAsync(
                            CookieAuthenticationDefaults.AuthenticationScheme);
                }

                return Task.CompletedTask;
            };
    });

builder.Services.AddAuthorization();

builder.Services.AddDistributedMemoryCache();

builder.Services.AddSession(options =>
{
    options.Cookie.Name =
        "BembosEvents.Session";

    options.Cookie.HttpOnly =
        true;

    options.Cookie.IsEssential =
        true;

    options.Cookie.SameSite =
        SameSiteMode.Lax;

    options.Cookie.SecurePolicy =
        CookieSecurePolicy.SameAsRequest;

    options.IdleTimeout =
        TimeSpan.FromHours(1);
});

builder.Services.AddHttpContextAccessor();

builder.Services.AddTransient<
    BackendTokenRefreshService>();

builder.Services.AddTransient<
    BackendBearerHandler>();

builder.Services
    .AddOptions<BackendApiOptions>()
    .Bind(
        builder.Configuration.GetSection(
            BackendApiOptions.SectionName))
    .Validate(
        options =>
            Uri.TryCreate(
                options.BaseUrl,
                UriKind.Absolute,
                out _),
        "BackendApi:BaseUrl debe ser una URL absoluta valida.")
    .ValidateOnStart();

builder.Services
    .AddHttpClient(
        "BackendAuthRefresh",
        (serviceProvider, httpClient) =>
        {
            var options =
                serviceProvider
                    .GetRequiredService<
                        IOptions<BackendApiOptions>>()
                    .Value;

            var baseUrl =
                options.BaseUrl.TrimEnd('/') + "/";

            httpClient.BaseAddress =
                new Uri(
                    baseUrl,
                    UriKind.Absolute);

            httpClient.Timeout =
                TimeSpan.FromSeconds(10);
        });
builder.Services
    .AddHttpClient<
        IBackendApiClient,
        BackendApiClient>(
        (serviceProvider, httpClient) =>
        {
            var options =
                serviceProvider
                    .GetRequiredService<
                        IOptions<BackendApiOptions>>()
                    .Value;

            var baseUrl =
                options.BaseUrl.TrimEnd('/') + "/";

            httpClient.BaseAddress =
                new Uri(
                    baseUrl,
                    UriKind.Absolute);

            httpClient.Timeout =
                TimeSpan.FromSeconds(10);
        })
    .AddHttpMessageHandler<
        BackendBearerHandler>();

builder.Services
    .AddHttpClient<
        IChatApiClient,
        ChatApiClient>(
        (serviceProvider, httpClient) =>
        {
            var options =
                serviceProvider
                    .GetRequiredService<
                        IOptions<BackendApiOptions>>()
                    .Value;

            var baseUrl =
                options.BaseUrl.TrimEnd('/') + "/";

            httpClient.BaseAddress =
                new Uri(
                    baseUrl,
                    UriKind.Absolute);

            httpClient.Timeout =
                TimeSpan.FromSeconds(15);
        })
    .AddHttpMessageHandler<
        BackendBearerHandler>();
builder.Services
    .AddHttpClient<
        IMercadoPagoApiClient,
        MercadoPagoApiClient>(
        (serviceProvider, httpClient) =>
        {
            var options =
                serviceProvider
                    .GetRequiredService<
                        IOptions<BackendApiOptions>>()
                    .Value;

            var baseUrl =
                options.BaseUrl.TrimEnd('/') + "/";

            httpClient.BaseAddress =
                new Uri(
                    baseUrl,
                    UriKind.Absolute);

            httpClient.Timeout =
                TimeSpan.FromSeconds(15);
        })
    .AddHttpMessageHandler<
        BackendBearerHandler>();

builder.Services
    .AddHealthChecks()
    .AddCheck<BackendApiHealthCheck>(
        "backend-api");

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler(
        "/Home/Error");

    app.UseHsts();
}

app.UseStaticFiles();

app.UseRouting();

app.UseSession();

app.UseAuthentication();

app.UseAuthorization();

app.MapHealthChecks(
    "/health/backend");

app.MapControllerRoute(
    name: "default",
    pattern:
        "{controller=Home}/{action=Index}/{id?}");

app.Run();
