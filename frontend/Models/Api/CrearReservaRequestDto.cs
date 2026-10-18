namespace BembosEvents.Web.Models.Api;

public sealed class CrearReservaRequestDto
{
    public int IdSede { get; set; }

    public int IdSalon { get; set; }

    public int? IdPaquete { get; set; }

    public int? IdTematica { get; set; }

    public DateOnly FechaEvento { get; set; }

    public TimeOnly HoraInicio { get; set; }

    public TimeOnly HoraFin { get; set; }

    public int CantidadInvitados { get; set; }

    public List<CrearReservaServicioRequestDto> ServiciosAdicionales
    {
        get;
        set;
    } = new();
}

public sealed class CrearReservaServicioRequestDto
{
    public int IdServicioAdicional { get; set; }

    public int Cantidad { get; set; }
}
