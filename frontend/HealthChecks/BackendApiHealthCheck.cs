using BembosEvents.Web.Services;
using Microsoft.Extensions.Diagnostics.HealthChecks;

namespace BembosEvents.Web.HealthChecks;

public sealed class BackendApiHealthCheck : IHealthCheck
{
    private readonly IBackendApiClient _backendApiClient;

    public BackendApiHealthCheck(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient = backendApiClient;
    }

    public async Task<HealthCheckResult> CheckHealthAsync(
        HealthCheckContext context,
        CancellationToken cancellationToken = default)
    {
        var result =
            await _backendApiClient.CheckConnectionAsync(
                cancellationToken);

        if (result.IsAvailable)
        {
            return HealthCheckResult.Healthy(
                result.Message);
        }

        return HealthCheckResult.Unhealthy(
            result.Message);
    }
}
