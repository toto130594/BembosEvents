using System.Net;
using BembosEvents.Web.Models.Auth;

namespace BembosEvents.Web.Services;

public sealed record BackendLoginResult(
    bool Success,
    AuthResponseDto? Auth,
    HttpStatusCode? StatusCode,
    string? ErrorMessage);
