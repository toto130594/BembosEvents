using System.Net;

namespace BembosEvents.Web.Services;

public sealed record BackendApiProbeResult(
    bool IsAvailable,
    HttpStatusCode? StatusCode,
    string Message);
