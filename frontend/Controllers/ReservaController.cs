using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using BembosEvents.Web.Extensions;
using BembosEvents.Web.Models.Reservations;
using BembosEvents.Web.Models.Api;
using BembosEvents.Web.Services;
using BembosEvents.Web.ViewModels;

namespace BembosEvents.Web.Controllers;

[AllowAnonymous]
[Route("reservar")]
public sealed class ReservaController : Controller
{
    private readonly IBackendApiClient _backendApiClient;

    public ReservaController(
        IBackendApiClient backendApiClient)
    {
        _backendApiClient =
            backendApiClient;
    }

    [HttpGet("")]
    public IActionResult Index()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        return RedirectToAction(
            GetNextAction(draft));
    }

    [HttpGet("sede")]
    public async Task<IActionResult> Sede()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        var sedes =
            await _backendApiClient.GetSedesAsync();

        return View(
            new ReservationSedeViewModel
            {
                IdSede =
                    draft.IdSede,

                Sedes =
                    sedes
            });
    }

    [HttpPost("sede")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Sede(
        ReservationSedeViewModel model)
    {
        var sedes =
            await _backendApiClient.GetSedesAsync();

        model.Sedes =
            sedes;

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var sede =
            sedes.FirstOrDefault(
                item =>
                    item.IdSede ==
                    model.IdSede!.Value);

        if (sede is null)
        {
            ModelState.AddModelError(
                nameof(model.IdSede),
                "La sede seleccionada no existe.");

            return View(model);
        }

        var draft =
            HttpContext.Session.GetReservationDraft();

        if (
            draft.IdSede !=
            sede.IdSede
        )
        {
            draft.IdSede =
                sede.IdSede;

            ResetAfterSedeChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Salon));
    }

    [HttpGet("salon")]
    public async Task<IActionResult> Salon()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.IdSede.HasValue)
        {
            return RedirectToAction(
                nameof(Sede));
        }

        var sede =
            await _backendApiClient.GetSedeAsync(
                draft.IdSede.Value);

        if (sede is null)
        {
            draft.IdSede =
                null;

            ResetAfterSedeChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return RedirectToAction(
                nameof(Sede));
        }

        var salones =
            await _backendApiClient.GetSalonesBySedeAsync(
                sede.IdSede);

        return View(
            new ReservationSalonViewModel
            {
                IdSede =
                    sede.IdSede,

                SedeNombre =
                    sede.Nombre,

                IdSalon =
                    draft.IdSalon,

                Salones =
                    salones
            });
    }

    [HttpPost("salon")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Salon(
        ReservationSalonViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.IdSede.HasValue)
        {
            return RedirectToAction(
                nameof(Sede));
        }

        var sede =
            await _backendApiClient.GetSedeAsync(
                draft.IdSede.Value);

        if (sede is null)
        {
            draft.IdSede =
                null;

            ResetAfterSedeChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return RedirectToAction(
                nameof(Sede));
        }

        var salones =
            await _backendApiClient.GetSalonesBySedeAsync(
                sede.IdSede);

        model.IdSede =
            sede.IdSede;

        model.SedeNombre =
            sede.Nombre;

        model.Salones =
            salones;

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                model.IdSalon!.Value);

        if (
            salon is null ||
            salon.IdSede != sede.IdSede
        )
        {
            ModelState.AddModelError(
                nameof(model.IdSalon),
                "El salon seleccionado no pertenece a la sede elegida.");

            return View(model);
        }

        if (
            !salones.Any(
                item =>
                    item.IdSalon ==
                    salon.IdSalon)
        )
        {
            ModelState.AddModelError(
                nameof(model.IdSalon),
                "El salon seleccionado no esta disponible para esta sede.");

            return View(model);
        }

        if (
            draft.IdSalon !=
            salon.IdSalon
        )
        {
            draft.IdSalon =
                salon.IdSalon;

            ResetAfterSalonChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(FechaHorario));
    }

    [HttpGet("fecha-horario")]
    public async Task<IActionResult> FechaHorario()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        var contextResult =
            await ResolveSalonContextAsync(
                draft);

        if (contextResult.Redirect is not null)
        {
            return contextResult.Redirect;
        }

        return View(
            new ReservationFechaHorarioViewModel
            {
                IdSalon =
                    contextResult.Salon!.IdSalon,

                SalonNombre =
                    contextResult.Salon.Nombre,

                FechaEvento =
                    draft.FechaEvento,

                HoraInicio =
                    draft.HoraInicio,

                HoraFin =
                    draft.HoraFin,

                Horarios =
                    contextResult.Horarios!
            });
    }

    [HttpPost("fecha-horario")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> FechaHorario(
        ReservationFechaHorarioViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        var contextResult =
            await ResolveSalonContextAsync(
                draft);

        if (contextResult.Redirect is not null)
        {
            return contextResult.Redirect;
        }

        model.IdSalon =
            contextResult.Salon!.IdSalon;

        model.SalonNombre =
            contextResult.Salon.Nombre;

        model.Horarios =
            contextResult.Horarios!;

        if (
            model.HoraInicio.HasValue &&
            model.HoraFin.HasValue &&
            model.HoraInicio.Value >=
            model.HoraFin.Value
        )
        {
            ModelState.AddModelError(
                nameof(model.HoraFin),
                "La hora de fin debe ser posterior a la hora de inicio.");
        }

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var fechaEvento =
            model.FechaEvento!.Value;

        var horaInicio =
            model.HoraInicio!.Value;

        var horaFin =
            model.HoraFin!.Value;

        var diaSemana =
            GetDiaSemana(
                fechaEvento);

        var dentroHorarioPublicado =
            model.Horarios.Any(
                horario =>
                    string.Equals(
                        horario.DiaSemana,
                        diaSemana,
                        StringComparison.OrdinalIgnoreCase) &&
                    horaInicio >=
                    TimeOnly.FromTimeSpan(
                        horario.HoraInicio) &&
                    horaFin <=
                    TimeOnly.FromTimeSpan(
                        horario.HoraFin));

        if (!dentroHorarioPublicado)
        {
            ModelState.AddModelError(
                string.Empty,
                "El horario seleccionado esta fuera del horario publicado para ese dia.");

            return View(model);
        }

        var disponibilidad =
            await _backendApiClient.GetDisponibilidadSalonAsync(
                model.IdSalon,
                fechaEvento,
                horaInicio,
                horaFin);

        if (
            disponibilidad is null ||
            !disponibilidad.Disponible
        )
        {
            var message =
                disponibilidad is not null &&
                !string.IsNullOrWhiteSpace(
                    disponibilidad.Motivo)
                    ? disponibilidad.Motivo
                    : "El horario seleccionado no esta disponible.";

            ModelState.AddModelError(
                string.Empty,
                message);

            return View(model);
        }

        var changed =
            draft.FechaEvento !=
            model.FechaEvento.Value ||
            draft.HoraInicio !=
            model.HoraInicio.Value ||
            draft.HoraFin !=
            model.HoraFin.Value;

        draft.FechaEvento =
            model.FechaEvento.Value;

        draft.HoraInicio =
            model.HoraInicio.Value;

        draft.HoraFin =
            model.HoraFin.Value;

        if (changed)
        {
            ResetAfterFechaHorarioChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Invitados));
    }

    [HttpGet("invitados")]
    public async Task<IActionResult> Invitados()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (
            !draft.FechaEvento.HasValue ||
            !draft.HoraInicio.HasValue ||
            !draft.HoraFin.HasValue ||
            !draft.IdSalon.HasValue
        )
        {
            return RedirectToAction(
                nameof(FechaHorario));
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                draft.IdSalon.Value);

        if (
            salon is null ||
            !draft.IdSede.HasValue ||
            salon.IdSede != draft.IdSede.Value
        )
        {
            draft.IdSalon =
                null;

            ResetAfterSalonChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return RedirectToAction(
                nameof(Salon));
        }

        return View(
            new ReservationInvitadosViewModel
            {
                SalonNombre =
                    salon.Nombre,

                CapacidadSalon =
                    salon.Capacidad,

                CantidadInvitados =
                    draft.CantidadInvitados
            });
    }

    [HttpPost("invitados")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Invitados(
        ReservationInvitadosViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (
            !draft.FechaEvento.HasValue ||
            !draft.HoraInicio.HasValue ||
            !draft.HoraFin.HasValue ||
            !draft.IdSalon.HasValue
        )
        {
            return RedirectToAction(
                nameof(FechaHorario));
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                draft.IdSalon.Value);

        if (
            salon is null ||
            !draft.IdSede.HasValue ||
            salon.IdSede != draft.IdSede.Value
        )
        {
            draft.IdSalon =
                null;

            ResetAfterSalonChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return RedirectToAction(
                nameof(Salon));
        }

        model.SalonNombre =
            salon.Nombre;

        model.CapacidadSalon =
            salon.Capacidad;

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var cantidad =
            model.CantidadInvitados!.Value;

        var changed =
            draft.CantidadInvitados !=
            cantidad;

        draft.CantidadInvitados =
            cantidad;

        if (changed)
        {
            ResetAfterInvitadosChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Paquete));
    }
    [HttpGet("paquete")]
    public async Task<IActionResult> Paquete()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (
            !draft.CantidadInvitados.HasValue ||
            draft.CantidadInvitados.Value <= 0
        )
        {
            return RedirectToAction(
                nameof(Invitados));
        }

        var paquetes =
            await _backendApiClient.GetPaquetesAsync();

        return View(
            new ReservationPaqueteViewModel
            {
                IdPaquete =
                    draft.IdPaquete,

                Paquetes =
                    paquetes
            });
    }

    [HttpPost("paquete")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Paquete(
        ReservationPaqueteViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (
            !draft.CantidadInvitados.HasValue ||
            draft.CantidadInvitados.Value <= 0
        )
        {
            return RedirectToAction(
                nameof(Invitados));
        }

        var paquetes =
            await _backendApiClient.GetPaquetesAsync();

        model.Paquetes =
            paquetes;

        if (
            model.IdPaquete.HasValue &&
            !paquetes.Any(
                item =>
                    item.IdPaquete ==
                    model.IdPaquete.Value)
        )
        {
            ModelState.AddModelError(
                nameof(model.IdPaquete),
                "El paquete seleccionado no existe.");
        }

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var changed =
            !draft.PaqueteCompletado ||
            draft.IdPaquete !=
            model.IdPaquete;

        draft.IdPaquete =
            model.IdPaquete;

        draft.PaqueteCompletado =
            true;

        if (changed)
        {
            ResetAfterPaqueteChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Tematica));
    }
    [HttpGet("tematica")]
    public async Task<IActionResult> Tematica()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.PaqueteCompletado)
        {
            return RedirectToAction(
                nameof(Paquete));
        }

        var tematicas =
            await _backendApiClient.GetTematicasAsync();

        return View(
            new ReservationTematicaViewModel
            {
                IdTematica =
                    draft.IdTematica,

                Tematicas =
                    tematicas
            });
    }

    [HttpPost("tematica")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Tematica(
        ReservationTematicaViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.PaqueteCompletado)
        {
            return RedirectToAction(
                nameof(Paquete));
        }

        var tematicas =
            await _backendApiClient.GetTematicasAsync();

        model.Tematicas =
            tematicas;

        if (
            model.IdTematica.HasValue &&
            !tematicas.Any(
                item =>
                    item.IdTematica ==
                    model.IdTematica.Value)
        )
        {
            ModelState.AddModelError(
                nameof(model.IdTematica),
                "La tematica seleccionada no existe.");
        }

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var changed =
            !draft.TematicaCompletada ||
            draft.IdTematica !=
            model.IdTematica;

        draft.IdTematica =
            model.IdTematica;

        draft.TematicaCompletada =
            true;

        if (changed)
        {
            ResetAfterTematicaChange(
                draft);
        }

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Servicios));
    }
    [HttpGet("servicios")]
    public async Task<IActionResult> Servicios()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.TematicaCompletada)
        {
            return RedirectToAction(
                nameof(Tematica));
        }

        var catalogo =
            await _backendApiClient.GetServiciosAdicionalesAsync();

        return View(
            new ReservationServiciosViewModel
            {
                Catalogo =
                    catalogo,

                Servicios =
                    draft.Servicios
                        .Select(
                            item =>
                                new ReservationServicioSeleccionViewModel
                                {
                                    IdServicioAdicional =
                                        item.IdServicioAdicional,

                                    Cantidad =
                                        item.Cantidad
                                })
                        .ToList()
            });
    }

    [HttpPost("servicios")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Servicios(
        ReservationServiciosViewModel model)
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        if (!draft.TematicaCompletada)
        {
            return RedirectToAction(
                nameof(Tematica));
        }

        var catalogo =
            await _backendApiClient.GetServiciosAdicionalesAsync();

        model.Catalogo =
            catalogo;

        model.Servicios ??=
            new List<ReservationServicioSeleccionViewModel>();

        var duplicateIds =
            model.Servicios
                .GroupBy(
                    item =>
                        item.IdServicioAdicional)
                .Where(
                    group =>
                        group.Count() > 1)
                .Select(
                    group =>
                        group.Key)
                .ToList();

        if (duplicateIds.Count > 0)
        {
            ModelState.AddModelError(
                string.Empty,
                "La solicitud contiene servicios duplicados.");
        }

        foreach (var item in model.Servicios)
        {
            if (
                item.IdServicioAdicional <= 0 ||
                !catalogo.Any(
                    servicio =>
                        servicio.IdServicioAdicional ==
                        item.IdServicioAdicional)
            )
            {
                ModelState.AddModelError(
                    string.Empty,
                    "Se detecto un servicio adicional invalido.");

                break;
            }
        }

        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var selected =
            model.Servicios
                .Where(
                    item =>
                        item.Cantidad > 0)
                .Select(
                    item =>
                        new ReservationServiceDraft
                        {
                            IdServicioAdicional =
                                item.IdServicioAdicional,

                            Cantidad =
                                item.Cantidad
                        })
                .OrderBy(
                    item =>
                        item.IdServicioAdicional)
                .ToList();

        draft.Servicios =
            selected;

        draft.ServiciosCompletados =
            true;

        HttpContext.Session.SetReservationDraft(
            draft);

        return RedirectToAction(
            nameof(Resumen));
    }
    [HttpGet("resumen")]
    public async Task<IActionResult> Resumen()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        var result =
            await BuildResumenAsync(
                draft);

        if (result.Redirect is not null)
        {
            return result.Redirect;
        }

        return View(
            result.ViewModel);
    }

    [HttpPost("confirmar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Confirmar()
    {
        var draft =
            HttpContext.Session.GetReservationDraft();

        var resumen =
            await BuildResumenAsync(
                draft);

        if (resumen.Redirect is not null)
        {
            return resumen.Redirect;
        }

        if (
            User.Identity?.IsAuthenticated !=
            true
        )
        {
            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=%2Freservar%2Fresumen");
        }

        var disponibilidad =
            await _backendApiClient.GetDisponibilidadSalonAsync(
                draft.IdSalon!.Value,
                draft.FechaEvento!.Value,
                draft.HoraInicio!.Value,
                draft.HoraFin!.Value);

        if (
            disponibilidad is null ||
            !disponibilidad.Disponible
        )
        {
            TempData["ReservationError"] =
                "El horario ya no esta disponible. Revisa la fecha y el horario antes de confirmar.";

            return RedirectToAction(
                nameof(FechaHorario));
        }

        var request =
            new BembosEvents.Web.Models.Api.CrearReservaRequestDto
            {
                IdSede =
                    draft.IdSede!.Value,

                IdSalon =
                    draft.IdSalon.Value,

                IdPaquete =
                    draft.IdPaquete,

                IdTematica =
                    draft.IdTematica,

                FechaEvento =
                    draft.FechaEvento.Value,

                HoraInicio =
                    draft.HoraInicio.Value,

                HoraFin =
                    draft.HoraFin.Value,

                CantidadInvitados =
                    draft.CantidadInvitados!.Value,

                ServiciosAdicionales =
                    draft.Servicios
                        .Select(
                            item =>
                                new BembosEvents.Web.Models.Api.CrearReservaServicioRequestDto
                                {
                                    IdServicioAdicional =
                                        item.IdServicioAdicional,

                                    Cantidad =
                                        item.Cantidad
                                })
                        .ToList()
            };

        var createResult =
            await _backendApiClient.CrearReservaAsync(
                request);

        if (
            createResult.StatusCode == 201 &&
            createResult.Data is not null
        )
        {
            var idReserva =
                createResult.Data.IdReserva;

            HttpContext.Session.ClearReservationDraft();

            return RedirectToAction(
                nameof(Confirmacion),
                new
                {
                    idReserva
                });
        }

        if (createResult.StatusCode == 401)
        {
            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=%2Freservar%2Fresumen");
        }

        if (createResult.StatusCode == 403)
        {
            TempData["ReservationError"] =
                "Tu cuenta no tiene permisos para crear reservas como cliente.";

            return RedirectToAction(
                nameof(Resumen));
        }

        if (createResult.StatusCode == 409)
        {
            TempData["ReservationError"] =
                "El horario acaba de dejar de estar disponible. Selecciona otro horario.";

            return RedirectToAction(
                nameof(FechaHorario));
        }

        if (createResult.StatusCode == 400)
        {
            TempData["ReservationError"] =
                "Spring Boot rechazo los datos de la reserva. Revisa el resumen antes de intentar nuevamente.";

            return RedirectToAction(
                nameof(Resumen));
        }

        if (createResult.StatusCode == 404)
        {
            TempData["ReservationError"] =
                "Uno de los elementos seleccionados ya no se encuentra disponible.";

            return RedirectToAction(
                nameof(Resumen));
        }

        TempData["ReservationError"] =
            "No fue posible crear la reserva en este momento. El borrador se conserva para volver a intentarlo.";

        return RedirectToAction(
            nameof(Resumen));
    }

    [HttpGet("confirmacion/{idReserva:int}")]
    public IActionResult Confirmacion(
        int idReserva)
    {
        if (
            User.Identity?.IsAuthenticated !=
            true
        )
        {
            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=%2Freservar%2Fresumen");
        }

        if (idReserva <= 0)
        {
            return RedirectToAction(
                nameof(Index));
        }

        return View(
            new ReservationConfirmacionViewModel
            {
                IdReserva =
                    idReserva
            });
    }

    [Authorize]
    [HttpGet("mis-reservas")]
    public async Task<IActionResult> MisReservas(
        CancellationToken cancellationToken)
    {
        var result =
            await _backendApiClient.GetMisReservasAsync(
                cancellationToken);

        if (result.StatusCode == 401)
        {
            ClearBackendAuthenticationSession();

            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=%2Freservar%2Fmis-reservas");
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (
            !result.IsSuccess ||
            result.Data is null
        )
        {
            return View(
                new MisReservasViewModel
                {
                    ErrorMessage =
                        "No fue posible consultar tus reservas en este momento."
                });
        }

        var items =
            new List<MisReservaItemViewModel>();

        foreach (var reserva in result.Data)
        {
            var sede =
                await _backendApiClient.GetSedeAsync(
                    reserva.IdSede,
                    cancellationToken);

            var salon =
                await _backendApiClient.GetSalonAsync(
                    reserva.IdSalon,
                    cancellationToken);

            items.Add(
                new MisReservaItemViewModel
                {
                    IdReserva =
                        reserva.IdReserva,

                    CodigoReserva =
                        reserva.CodigoReserva,

                    Estado =
                        reserva.Estado,

                    SedeNombre =
                        sede?.Nombre ??
                        $"Sede #{reserva.IdSede}",

                    SalonNombre =
                        salon?.Nombre ??
                        $"Salon #{reserva.IdSalon}",

                    FechaEvento =
                        reserva.FechaEvento,

                    HoraInicio =
                        reserva.HoraInicio,

                    HoraFin =
                        reserva.HoraFin,

                    CantidadInvitados =
                        reserva.CantidadInvitados,

                    MontoTotal =
                        reserva.MontoTotal,

                    MontoDeposito =
                        reserva.MontoDeposito,

                    FechaCreacion =
                        reserva.FechaCreacion
                });
        }

        return View(
            new MisReservasViewModel
            {
                Reservas =
                    items
            });
    }

    [Authorize]
    [HttpGet("mis-reservas/{idReserva:int}")]
    public async Task<IActionResult> MisReservaDetalle(
        int idReserva,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient.GetReservaPropiaAsync(
                idReserva,
                cancellationToken);

        if (result.StatusCode == 401)
        {
            ClearBackendAuthenticationSession();

            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=" +
                Uri.EscapeDataString(
                    $"/reservar/mis-reservas/{idReserva}"));
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (
            result.StatusCode == 404 ||
            result.Data is null
        )
        {
            return NotFound();
        }

        if (!result.IsSuccess)
        {
            TempData["ReservationError"] =
                "No fue posible consultar el detalle de la reserva.";

            return RedirectToAction(
                nameof(MisReservas));
        }

        var reserva =
            result.Data;

        var sede =
            await _backendApiClient.GetSedeAsync(
                reserva.IdSede,
                cancellationToken);

        var salon =
            await _backendApiClient.GetSalonAsync(
                reserva.IdSalon,
                cancellationToken);

        PaquetePublicoDto? paquete =
            null;

        if (reserva.IdPaquete.HasValue)
        {
            paquete =
                await _backendApiClient.GetPaqueteAsync(
                    reserva.IdPaquete.Value,
                    cancellationToken);
        }

        TematicaPublicaDto? tematica =
            null;

        if (reserva.IdTematica.HasValue)
        {
            tematica =
                await _backendApiClient.GetTematicaAsync(
                    reserva.IdTematica.Value,
                    cancellationToken);
        }

        var servicios =
            new List<ReservaDetalleServicioViewModel>();

        foreach (
            var detalle in
            reserva.ServiciosAdicionales
        )
        {
            var catalogo =
                await _backendApiClient.GetServicioAdicionalAsync(
                    detalle.IdServicioAdicional,
                    cancellationToken);

            servicios.Add(
                new ReservaDetalleServicioViewModel
                {
                    IdServicioAdicional =
                        detalle.IdServicioAdicional,

                    Nombre =
                        catalogo?.Nombre ??
                        $"Servicio #{detalle.IdServicioAdicional}",

                    Cantidad =
                        detalle.Cantidad,

                    PrecioUnitario =
                        detalle.PrecioUnitario,

                    Subtotal =
                        detalle.Subtotal
                });
        }

        return View(
            "MisReservaDetalle",
            new ReservaDetalleViewModel
            {
                IdReserva =
                    reserva.IdReserva,

                CodigoReserva =
                    reserva.CodigoReserva,

                Estado =
                    reserva.Estado,

                SedeNombre =
                    sede?.Nombre ??
                    $"Sede #{reserva.IdSede}",

                SalonNombre =
                    salon?.Nombre ??
                    $"Salon #{reserva.IdSalon}",

                PaqueteNombre =
                    paquete?.Nombre,

                TematicaNombre =
                    tematica?.Nombre,

                FechaEvento =
                    reserva.FechaEvento,

                HoraInicio =
                    reserva.HoraInicio,

                HoraFin =
                    reserva.HoraFin,

                CantidadInvitados =
                    reserva.CantidadInvitados,

                MontoBase =
                    reserva.MontoBase,

                MontoServicios =
                    reserva.MontoServicios,

                MontoTotal =
                    reserva.MontoTotal,

                PorcentajeDeposito =
                    reserva.PorcentajeDeposito,

                MontoDeposito =
                    reserva.MontoDeposito,

                FechaCreacion =
                    reserva.FechaCreacion,

                FechaActualizacion =
                    reserva.FechaActualizacion,

                Servicios =
                    servicios
            });
    }

    [Authorize]
    [HttpPost("mis-reservas/{idReserva:int}/cancelar")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> CancelarPropia(
        int idReserva,
        CancellationToken cancellationToken)
    {
        if (idReserva <= 0)
        {
            return NotFound();
        }

        var result =
            await _backendApiClient.CancelarReservaPropiaAsync(
                idReserva,
                cancellationToken);

        if (result.StatusCode == 401)
        {
            ClearBackendAuthenticationSession();

            return Redirect(
                "/cuenta/iniciar-sesion?ReturnUrl=" +
                Uri.EscapeDataString(
                    $"/reservar/mis-reservas/{idReserva}"));
        }

        if (result.StatusCode == 403)
        {
            return RedirectToAction(
                "AccessDenied",
                "Account");
        }

        if (result.StatusCode == 404)
        {
            return NotFound();
        }

        if (result.StatusCode == 409)
        {
            TempData["ReservationError"] =
                "La reserva ya no se encuentra en un estado que permita cancelarla.";

            return RedirectToAction(
                nameof(MisReservaDetalle),
                new
                {
                    idReserva
                });
        }

        if (result.StatusCode == 400)
        {
            TempData["ReservationError"] =
                "Spring Boot rechazo la cancelacion de la reserva.";

            return RedirectToAction(
                nameof(MisReservaDetalle),
                new
                {
                    idReserva
                });
        }

        if (
            !result.IsSuccess ||
            result.Data is null
        )
        {
            TempData["ReservationError"] =
                "No fue posible cancelar la reserva en este momento.";

            return RedirectToAction(
                nameof(MisReservaDetalle),
                new
                {
                    idReserva
                });
        }

        TempData["ReservationSuccess"] =
            "Reserva cancelada correctamente.";

        return RedirectToAction(
            nameof(MisReservaDetalle),
            new
            {
                idReserva
            });
    }
    private void ClearBackendAuthenticationSession()
    {
        HttpContext.Session.Remove(
            BembosEvents.Web.Security.FrontendSessionKeys.BackendJwt);

        HttpContext.Session.Remove(
            BembosEvents.Web.Security.FrontendSessionKeys.BackendJwtExpiresAtUtc);
    }
    private async Task<ReservationResumenBuildResult> BuildResumenAsync(
        ReservationDraft draft)
    {
        var nextAction =
            GetNextAction(
                draft);

        if (
            nextAction !=
            nameof(Resumen)
        )
        {
            return new ReservationResumenBuildResult(
                null,
                RedirectToAction(
                    nextAction));
        }

        var sede =
            await _backendApiClient.GetSedeAsync(
                draft.IdSede!.Value);

        if (sede is null)
        {
            draft.IdSede =
                null;

            ResetAfterSedeChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return new ReservationResumenBuildResult(
                null,
                RedirectToAction(
                    nameof(Sede)));
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                draft.IdSalon!.Value);

        if (
            salon is null ||
            salon.IdSede != sede.IdSede
        )
        {
            draft.IdSalon =
                null;

            ResetAfterSalonChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return new ReservationResumenBuildResult(
                null,
                RedirectToAction(
                    nameof(Salon)));
        }

        var paquetes =
            await _backendApiClient.GetPaquetesAsync();

         PaquetePublicoDto? paquete =

             null;

        if (draft.IdPaquete.HasValue)
        {
            paquete =
                paquetes.FirstOrDefault(
                    item =>
                        item.IdPaquete ==
                        draft.IdPaquete.Value);

            if (paquete is null)
            {
                draft.IdPaquete =
                    null;

                draft.PaqueteCompletado =
                    false;

                ResetAfterPaqueteChange(
                    draft);

                HttpContext.Session.SetReservationDraft(
                    draft);

                return new ReservationResumenBuildResult(
                    null,
                    RedirectToAction(
                        nameof(Paquete)));
            }
        }

        var tematicas =
            await _backendApiClient.GetTematicasAsync();

         TematicaPublicaDto? tematica =

             null;

        if (draft.IdTematica.HasValue)
        {
            tematica =
                tematicas.FirstOrDefault(
                    item =>
                        item.IdTematica ==
                        draft.IdTematica.Value);

            if (tematica is null)
            {
                draft.IdTematica =
                    null;

                draft.TematicaCompletada =
                    false;

                ResetAfterTematicaChange(
                    draft);

                HttpContext.Session.SetReservationDraft(
                    draft);

                return new ReservationResumenBuildResult(
                    null,
                    RedirectToAction(
                        nameof(Tematica)));
            }
        }

        var catalogoServicios =
            await _backendApiClient.GetServiciosAdicionalesAsync();

        var servicios =
            new List<ReservationResumenServicioViewModel>();

        foreach (var selected in draft.Servicios)
        {
            var servicio =
                catalogoServicios.FirstOrDefault(
                    item =>
                        item.IdServicioAdicional ==
                        selected.IdServicioAdicional);

            if (servicio is null)
            {
                draft.Servicios.Clear();

                draft.ServiciosCompletados =
                    false;

                HttpContext.Session.SetReservationDraft(
                    draft);

                return new ReservationResumenBuildResult(
                    null,
                    RedirectToAction(
                        nameof(Servicios)));
            }

            servicios.Add(
                new ReservationResumenServicioViewModel
                {
                    Nombre =
                        servicio.Nombre,

                    Cantidad =
                        selected.Cantidad,

                    PrecioUnitario =
                        servicio.Precio
                });
        }

        var viewModel =
            new ReservationResumenViewModel
            {
                SedeNombre =
                    sede.Nombre,

                SalonNombre =
                    salon.Nombre,

                FechaEvento =
                    draft.FechaEvento!.Value,

                HoraInicio =
                    draft.HoraInicio!.Value,

                HoraFin =
                    draft.HoraFin!.Value,

                CantidadInvitados =
                    draft.CantidadInvitados!.Value,

                PaqueteNombre =
                    paquete?.Nombre,

                PaquetePrecioBase =
                    paquete?.PrecioBase,

                TematicaNombre =
                    tematica?.Nombre,

                TematicaPrecioAdicional =
                    tematica?.PrecioAdicional,

                Servicios =
                    servicios
            };

        return new ReservationResumenBuildResult(
            viewModel,
            null);
    }
    private async Task<SalonContextResult> ResolveSalonContextAsync(
        ReservationDraft draft)
    {
        if (!draft.IdSede.HasValue)
        {
            return new SalonContextResult(
                null,
                null,
                RedirectToAction(
                    nameof(Sede)));
        }

        if (!draft.IdSalon.HasValue)
        {
            return new SalonContextResult(
                null,
                null,
                RedirectToAction(
                    nameof(Salon)));
        }

        var salon =
            await _backendApiClient.GetSalonAsync(
                draft.IdSalon.Value);

        if (
            salon is null ||
            salon.IdSede != draft.IdSede.Value
        )
        {
            draft.IdSalon =
                null;

            ResetAfterSalonChange(
                draft);

            HttpContext.Session.SetReservationDraft(
                draft);

            return new SalonContextResult(
                null,
                null,
                RedirectToAction(
                    nameof(Salon)));
        }

        var horarios =
            await _backendApiClient.GetHorariosSalonAsync(
                salon.IdSalon);

        return new SalonContextResult(
            salon,
            horarios,
            null);
    }


    private static string GetDiaSemana(
        DateOnly fecha)
    {
        return fecha.DayOfWeek switch
        {
            DayOfWeek.Monday =>
                "LUNES",

            DayOfWeek.Tuesday =>
                "MARTES",

            DayOfWeek.Wednesday =>
                "MIERCOLES",

            DayOfWeek.Thursday =>
                "JUEVES",

            DayOfWeek.Friday =>
                "VIERNES",

            DayOfWeek.Saturday =>
                "SABADO",

            DayOfWeek.Sunday =>
                "DOMINGO",

            _ =>
                throw new InvalidOperationException(
                    "Dia de semana no soportado.")
        };
    }

    private static void ResetAfterSedeChange(
        ReservationDraft draft)
    {
        draft.IdSalon =
            null;

        draft.FechaEvento =
            null;

        draft.HoraInicio =
            null;

        draft.HoraFin =
            null;

        draft.CantidadInvitados =
            null;

        draft.IdPaquete =
            null;

        draft.PaqueteCompletado =
            false;

        draft.IdTematica =
            null;

        draft.TematicaCompletada =
            false;

        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }

    private static void ResetAfterSalonChange(
        ReservationDraft draft)
    {
        draft.FechaEvento =
            null;

        draft.HoraInicio =
            null;

        draft.HoraFin =
            null;

        draft.CantidadInvitados =
            null;

        draft.IdPaquete =
            null;

        draft.PaqueteCompletado =
            false;

        draft.IdTematica =
            null;

        draft.TematicaCompletada =
            false;

        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }

    private static void ResetAfterFechaHorarioChange(
        ReservationDraft draft)
    {
        draft.CantidadInvitados =
            null;

        draft.IdPaquete =
            null;

        draft.PaqueteCompletado =
            false;

        draft.IdTematica =
            null;

        draft.TematicaCompletada =
            false;

        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }

    private static void ResetAfterInvitadosChange(
        ReservationDraft draft)
    {
        draft.IdPaquete =
            null;

        draft.PaqueteCompletado =
            false;

        draft.IdTematica =
            null;

        draft.TematicaCompletada =
            false;

        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }
    private static void ResetAfterPaqueteChange(
        ReservationDraft draft)
    {
        draft.IdTematica =
            null;

        draft.TematicaCompletada =
            false;

        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }
    private static void ResetAfterTematicaChange(
        ReservationDraft draft)
    {
        draft.Servicios.Clear();

        draft.ServiciosCompletados =
            false;
    }
    private static string GetNextAction(
        ReservationDraft draft)
    {
        if (!draft.IdSede.HasValue)
        {
            return nameof(Sede);
        }

        if (!draft.IdSalon.HasValue)
        {
            return nameof(Salon);
        }

        if (
            !draft.FechaEvento.HasValue ||
            !draft.HoraInicio.HasValue ||
            !draft.HoraFin.HasValue
        )
        {
            return nameof(FechaHorario);
        }

        if (
            !draft.CantidadInvitados.HasValue ||
            draft.CantidadInvitados.Value <= 0
        )
        {
            return nameof(Invitados);
        }

        if (!draft.PaqueteCompletado)
        {
            return nameof(Paquete);
        }

        if (!draft.TematicaCompletada)
        {
            return nameof(Tematica);
        }

        if (!draft.ServiciosCompletados)
        {
            return nameof(Servicios);
        }

        return nameof(Resumen);
    }

    private sealed record ReservationResumenBuildResult(
        ReservationResumenViewModel? ViewModel,
        IActionResult? Redirect);
    private sealed record SalonContextResult(
        BembosEvents.Web.Models.Api.SalonPublicoDto? Salon,
        IReadOnlyList<BembosEvents.Web.Models.Api.HorarioSalonDto>? Horarios,
        IActionResult? Redirect);
}
