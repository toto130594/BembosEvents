using System.Net;

namespace BembosEvents.Web.Services;

public sealed record BackendSecurityActionResult(
    bool Success,
    HttpStatusCode? StatusCode,
    string? Message);