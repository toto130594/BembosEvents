namespace BembosEvents.Web.ViewModels;

public sealed class MisReservasViewModel
{
    public IReadOnlyList<MisReservaItemViewModel> Reservas
    {
        get;
        set;
    } = Array.Empty<MisReservaItemViewModel>();

    public string? ErrorMessage { get; set; }
}

public sealed class MisReservaItemViewModel
{
    public int IdReserva { get; set; }

    public string CodigoReserva { get; set; } =
        string.Empty;

    public string Estado { get; set; } =
        string.Empty;

    public string SedeNombre { get; set; } =
        string.Empty;

    public string SalonNombre { get; set; } =
        string.Empty;

    public DateOnly FechaEvento { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public int CantidadInvitados { get; set; }

    public decimal MontoTotal { get; set; }

    public decimal MontoDeposito { get; set; }

    public DateTime FechaCreacion { get; set; }
}
