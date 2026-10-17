using System.Net.Http.Headers;

namespace BembosEvents.Web.Security;

public sealed class BackendBearerHandler : DelegatingHandler
{
    private readonly IHttpContextAccessor _httpContextAccessor;

    public BackendBearerHandler(
        IHttpContextAccessor httpContextAccessor)
    {
        _httpContextAccessor = httpContextAccessor;
    }

    protected override Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request,
        CancellationToken cancellationToken)
    {
        var path =
            request.RequestUri?.AbsolutePath ??
            string.Empty;

        if (
            path.StartsWith(
                "/api/auth/",
                StringComparison.OrdinalIgnoreCase)
        )
        {
            request.Headers.Authorization = null;

            return base.SendAsync(
                request,
                cancellationToken);
        }

        var httpContext =
            _httpContextAccessor.HttpContext;

        var token =
            httpContext?
                .Session
                .GetString(
                    FrontendSessionKeys.BackendJwt);

        if (!string.IsNullOrWhiteSpace(token))
        {
            request.Headers.Authorization =
                new AuthenticationHeaderValue(
                    "Bearer",
                    token);
        }

        return base.SendAsync(
            request,
            cancellationToken);
    }
}
