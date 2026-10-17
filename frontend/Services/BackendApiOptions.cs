namespace BembosEvents.Web.Services;

public sealed class BackendApiOptions
{
    public const string SectionName = "BackendApi";

    public string BaseUrl { get; init; } = string.Empty;
}
