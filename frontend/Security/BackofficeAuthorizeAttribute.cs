using Microsoft.AspNetCore.Mvc;

namespace BembosEvents.Web.Security;

[AttributeUsage(
    AttributeTargets.Class |
    AttributeTargets.Method,
    AllowMultiple = false,
    Inherited = true)]
public sealed class BackofficeAuthorizeAttribute :
    TypeFilterAttribute
{
    public BackofficeAuthorizeAttribute()
        : base(typeof(BackofficeAccessFilter))
    {
        Order = -100;
    }
}
