using System.Net;
using BembosEvents.Web.Models.Auth;

namespace BembosEvents.Web.Services;

public sealed record BackendSessionListResult(
    bool Success,
    IReadOnlyList<BackendSessionDto> Sessions,
    HttpStatusCode? StatusCode,
    string? Message);