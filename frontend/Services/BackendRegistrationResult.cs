using System.Net;

namespace BembosEvents.Web.Services;

public sealed record BackendRegistrationResult(
    bool Success,
    HttpStatusCode? StatusCode,
    string? Message);
