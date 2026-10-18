using System.Text.Json;
using BembosEvents.Web.Models.Reservations;
using BembosEvents.Web.Security;

namespace BembosEvents.Web.Extensions;

public static class ReservationSessionExtensions
{
    private static readonly JsonSerializerOptions JsonOptions =
        new(JsonSerializerDefaults.Web);

    public static ReservationDraft GetReservationDraft(
        this ISession session)
    {
        ArgumentNullException.ThrowIfNull(session);

        var json =
            session.GetString(
                FrontendSessionKeys.ReservationDraft);

        if (string.IsNullOrWhiteSpace(json))
        {
            return new ReservationDraft();
        }

        try
        {
            return JsonSerializer.Deserialize<ReservationDraft>(
                       json,
                       JsonOptions)
                   ?? new ReservationDraft();
        }
        catch (JsonException)
        {
            session.Remove(
                FrontendSessionKeys.ReservationDraft);

            return new ReservationDraft();
        }
    }

    public static void SetReservationDraft(
        this ISession session,
        ReservationDraft draft)
    {
        ArgumentNullException.ThrowIfNull(session);
        ArgumentNullException.ThrowIfNull(draft);

        var json =
            JsonSerializer.Serialize(
                draft,
                JsonOptions);

        session.SetString(
            FrontendSessionKeys.ReservationDraft,
            json);
    }

    public static void ClearReservationDraft(
        this ISession session)
    {
        ArgumentNullException.ThrowIfNull(session);

        session.Remove(
            FrontendSessionKeys.ReservationDraft);
    }
}
