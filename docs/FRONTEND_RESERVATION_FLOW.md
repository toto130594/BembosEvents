# Bembos Events - Flujo MVC de Reserva

## 1. Objetivo

Definir el flujo del frontend ASP.NET Core MVC para que un cliente pueda
preparar y confirmar una reserva consumiendo exclusivamente la API
Spring Boot.

El frontend no accede a SQL Server directamente y no replica las reglas
de negocio como autoridad.

Spring Boot continua siendo la fuente de verdad para:

- identidad del usuario;
- rol CLIENTE;
- relacion Sede -> Salon;
- capacidad;
- horarios;
- disponibilidad;
- conflictos de reservas;
- precios;
- deposito;
- montos finales;
- estados de reserva.

---

## 2. Contrato de creacion

La confirmacion final utiliza:

POST /api/reservas

El backend exige rol CLIENTE.

El request contiene:

- idSede: obligatorio;
- idSalon: obligatorio;
- idPaquete: opcional;
- idTematica: opcional;
- fechaEvento: obligatoria;
- horaInicio: obligatoria;
- horaFin: obligatoria;
- cantidadInvitados: obligatoria y positiva;
- serviciosAdicionales: lista de servicios seleccionados.

Cada servicio seleccionado contiene:

- idServicioAdicional;
- cantidad.

El frontend no envia:

- idCliente;
- correo como identidad;
- rol;
- precio unitario;
- monto base;
- monto total;
- monto deposito;
- estado inicial de la reserva.

Esos valores corresponden al backend.

---

## 3. Flujo visual definitivo

El wizard queda definido asi:

1. Sede
2. Salon
3. Fecha y horario
4. Invitados
5. Paquete
6. Tematica
7. Servicios adicionales
8. Resumen
9. Confirmacion

Paquete y Tematica son pasos opcionales.

Servicios adicionales tambien pueden quedar sin seleccion.

El usuario siempre debe poder continuar con:

- sin paquete;
- sin tematica;
- sin servicios adicionales;

siempre que el backend acepte la reserva segun sus reglas vigentes.

---

## 4. Rutas MVC propuestas

### Entrada

GET /reservar

Responsabilidad:

- crear o recuperar un borrador;
- determinar el primer paso incompleto;
- redirigir al paso correspondiente.

### Sede

GET /reservar/sede

POST /reservar/sede

Consume:

GET /api/sedes

Al cambiar la sede se debe limpiar:

- idSalon;
- fechaEvento;
- horaInicio;
- horaFin.

Esto evita conservar un salon perteneciente a otra sede.

### Salon

GET /reservar/salon

POST /reservar/salon

Consume:

GET /api/sedes/{idSede}/salones

Nunca se utiliza un listado global de salones.

Al cambiar el salon se debe limpiar:

- fechaEvento;
- horaInicio;
- horaFin.

### Fecha y horario

GET /reservar/fecha-horario

POST /reservar/fecha-horario

Consume:

GET /api/salones/{idSalon}/horarios

y para validar una seleccion:

GET /api/salones/{idSalon}/disponibilidad
    ?fecha=YYYY-MM-DD
    &horaInicio=HH:mm
    &horaFin=HH:mm

El frontend debe utilizar los horarios publicados para el salon.

No debe inventar horarios.

La disponibilidad mostrada al usuario es una consulta previa.
No constituye una reserva temporal del horario.

### Invitados

GET /reservar/invitados

POST /reservar/invitados

La cantidad debe ser positiva.

La capacidad del salon puede utilizarse para prevalidacion de UX,
pero Spring Boot continua siendo la validacion definitiva.

### Paquete

GET /reservar/paquete

POST /reservar/paquete

Consume:

GET /api/paquetes

Debe existir la opcion:

Sin paquete

Si el catalogo esta vacio, el flujo no debe bloquearse.

### Tematica

GET /reservar/tematica

POST /reservar/tematica

Consume:

GET /api/tematicas

Debe existir la opcion:

Sin tematica

Si el catalogo esta vacio, el flujo no debe bloquearse.

### Servicios adicionales

GET /reservar/servicios

POST /reservar/servicios

Consume:

GET /api/servicios-adicionales

Cada seleccion conserva:

- idServicioAdicional;
- cantidad.

Cantidad debe ser positiva cuando el servicio esta seleccionado.

Debe ser valido continuar sin servicios.

### Resumen

GET /reservar/resumen

El resumen vuelve a consultar los catalogos mediante los IDs
almacenados en el borrador.

No debe confiar en nombres ni precios enviados previamente por
el navegador.

Debe mostrar:

- sede;
- salon;
- fecha;
- hora inicio;
- hora fin;
- invitados;
- paquete o "Sin paquete";
- tematica o "Sin tematica";
- servicios seleccionados.

---

## 5. Montos antes de confirmar

No existe en el contrato auditado un endpoint especifico de cotizacion
de reserva.

Por este motivo, el MVC no debe implementar una segunda version de la
logica de precios del backend.

Antes de confirmar se pueden mostrar los precios actuales de catalogo
como informacion, pero no deben presentarse como monto final
autoritativo.

Los importes definitivos se reciben de POST /api/reservas:

- montoBase;
- montoServicios;
- montoTotal;
- porcentajeDeposito;
- montoDeposito.

---

## 6. Confirmacion

POST /reservar/confirmar

Debe usar antiforgery.

Antes de enviar la reserva se debe volver a consultar:

GET /api/salones/{idSalon}/disponibilidad

Esto reduce conflictos entre la ultima pantalla y la confirmacion.

Despues se envia:

POST /api/reservas

El JWT se agrega exclusivamente mediante BackendBearerHandler.

El JWT nunca se envia al navegador mediante:

- localStorage;
- sessionStorage;
- JavaScript;
- hidden fields.

---

## 7. Autenticacion

Los catalogos y la preparacion del borrador pueden permanecer
disponibles para usuarios anonimos.

POST /api/reservas requiere CLIENTE.

Al llegar a la confirmacion:

### Usuario anonimo

Redirigir a:

/cuenta/iniciar-sesion?ReturnUrl=/reservar/resumen

El borrador permanece en Session del servidor.

Despues del login el usuario puede regresar al resumen.

### Usuario autenticado

El MVC no inventa ni almacena un claim de rol CLIENTE.

Se envia la solicitud al backend.

Spring Boot decide si la identidad autenticada tiene rol CLIENTE.

### Usuario autenticado sin rol CLIENTE

Spring Boot puede responder 403.

El MVC debe mostrar un mensaje apropiado y no transformar ese 403
en una reserva valida.

---

## 8. Estado temporal del wizard

Usar Session server-side.

Clave propuesta:

BembosEvents.ReservationDraft

El borrador contiene solamente:

- int? IdSede;
- int? IdSalon;
- DateOnly? FechaEvento;
- TimeOnly? HoraInicio;
- TimeOnly? HoraFin;
- int? CantidadInvitados;
- int? IdPaquete;
- int? IdTematica;
- lista de IdServicioAdicional + Cantidad.

No guardar en el borrador:

- JWT;
- roles;
- nombres como fuente de verdad;
- precios como fuente de verdad;
- montos calculados;
- estado de reserva.

Los nombres y precios se rehidratan desde Spring Boot.

---

## 9. Dependencias entre pasos

El servidor debe impedir avanzar si faltan pasos anteriores.

Ejemplos:

/reservar/salon requiere IdSede.

/reservar/fecha-horario requiere IdSalon.

/reservar/invitados requiere fecha y horario validos.

/reservar/resumen requiere todos los campos obligatorios.

No se debe confiar solamente en ocultar botones desde HTML.

---

## 10. Disponibilidad y concurrencia

La consulta de disponibilidad no bloquea el horario.

Dos clientes pueden consultar el mismo horario antes de confirmar.

Por eso:

1. consultar disponibilidad durante el paso fecha-horario;
2. consultar nuevamente antes de POST /api/reservas;
3. aun asi aceptar que Spring Boot pueda detectar un conflicto;
4. si POST /api/reservas responde conflicto, no crear datos locales;
5. conservar el borrador;
6. regresar a Fecha y horario con el mensaje del backend.

Spring Boot es la ultima autoridad frente a condiciones de carrera.

---

## 11. Creacion exitosa

POST /api/reservas responde HTTP 201.

La respuesta contiene al menos:

- idReserva;
- codigoReserva;
- estado;
- idSede;
- idSalon;
- idPaquete;
- idTematica;
- fechaEvento;
- horaInicio;
- horaFin;
- cantidadInvitados;
- montoBase;
- montoServicios;
- montoTotal;
- porcentajeDeposito;
- montoDeposito;
- fechaCreacion.

Despues de una creacion exitosa:

1. limpiar BembosEvents.ReservationDraft;
2. no repetir el POST al refrescar;
3. utilizar Post/Redirect/Get;
4. redirigir al detalle de la reserva creada.

Ruta objetivo propuesta:

GET /mis-reservas/{idReserva}

---

## 12. Mis reservas

El mismo modulo posteriormente utiliza:

GET /api/reservas/mias

para:

GET /mis-reservas

y:

GET /api/reservas/{idReserva}

para:

GET /mis-reservas/{idReserva}

El backend valida que la reserva pertenezca al cliente autenticado.

El navegador nunca envia idCliente como fuente de autorizacion.

---

## 13. Cancelacion

Ruta MVC propuesta:

POST /mis-reservas/{idReserva}/cancelar

Consume:

POST /api/reservas/{idReserva}/cancelacion

Debe usar:

- autenticacion;
- antiforgery;
- JWT server-side.

Spring Boot decide si el estado actual admite cancelacion.

Un conflicto de transicion debe mostrarse como regla de negocio,
no como error 500.

---

## 14. Pagos

Los pagos no forman parte del POST que crea la reserva.

Son un flujo posterior.

Contrato disponible:

POST /api/reservas/{idReserva}/pagos

GET /api/reservas/{idReserva}/pagos

Ambos pertenecen al cliente autenticado.

La implementacion del pago debe comenzar despues de tener funcionando:

- crear reserva;
- mis reservas;
- detalle de reserva.

El monto de deposito recibido de la reserva puede utilizarse como
referencia visual, pero las reglas del PagoService siguen siendo
autoritativas.

---

## 15. Manejo de errores

### 400

Datos invalidos.

Mantener borrador y mostrar validaciones.

### 401

Sesion/JWT ausente o vencido.

Redirigir al login conservando ReturnUrl.

### 403

Usuario autenticado pero sin permiso.

No inventar rol en MVC.

### 404

Recurso inexistente o no accesible segun el contrato.

### 409

Conflicto de negocio.

Ejemplos posibles:

- horario ya ocupado;
- transicion no permitida;
- cancelacion no permitida.

El mensaje del backend debe conservarse cuando sea seguro mostrarlo.

### 500 / indisponibilidad

Mostrar error recuperable.

Nunca borrar el borrador por un fallo de infraestructura.

---

## 16. Seguridad

Todos los POST MVC usan antiforgery.

El JWT continua exclusivamente en Session server-side.

No utilizar:

- localStorage;
- sessionStorage del navegador;
- cookies JavaScript;
- campos hidden para JWT;
- claims de rol inventados por MVC.

No enviar idCliente desde el browser.

No aceptar precios enviados por el navegador.

No confiar en idSalon sin volver a comprobar la sede mediante
los endpoints y reglas del backend.

---

## 17. UX

El wizard debe mostrar progreso:

Sede
-> Salon
-> Fecha y horario
-> Invitados
-> Paquete
-> Tematica
-> Servicios
-> Resumen
-> Confirmacion

Cada paso debe incluir:

- Volver;
- Continuar;
- resumen corto de selecciones previas;
- estado vacio cuando un catalogo opcional no tenga elementos.

En mobile el indicador de progreso puede ser compacto.

No mostrar todos los campos como un formulario tecnico unico.

---

## 18. Responsive

Mobile-first.

Breakpoints del Design System:

- 576px;
- 768px;
- 992px;
- 1200px.

Los elementos seleccionables deben utilizar tarjetas o controles
grandes y accesibles.

Los botones principales deben ser faciles de pulsar en movil.

---

## 19. Accesibilidad

- labels asociados a inputs;
- mensajes de validacion legibles por lectores de pantalla;
- focus visible;
- botones reales para acciones;
- no depender solamente de color;
- estado seleccionado detectable semanticamente;
- respetar prefers-reduced-motion.

---

## 20. Fases de implementacion

### 8.0.9C

Base tecnica:

- ReservationDraft;
- Session helpers;
- DTOs request/response;
- extension de IBackendApiClient;
- ReservaController base;
- rutas del wizard;
- sin crear reservas reales todavia.

### 8.0.9D

Sede + Salon.

### 8.0.9E

Fecha + Horario + Disponibilidad.

### 8.0.9F

Invitados + Paquete + Tematica + Servicios.

### 8.0.9G

Resumen + autenticacion + POST real de reserva.

### 8.0.9H

Mis reservas + detalle.

### 8.0.9I

Cancelacion propia.

### 8.0.9J

Cierre y regresion integral.

Pagos quedan para una fase posterior al cierre funcional de reservas.

---

## 21. Principios de cierre

La fase de reservas sera considerada completa cuando:

- el usuario pueda construir un borrador;
- sede y salon respeten su relacion;
- fecha y horario se validen contra disponibilidad;
- invitados sean obligatorios;
- paquete sea opcional;
- tematica sea opcional;
- servicios sean opcionales;
- un CLIENTE autenticado pueda confirmar;
- Spring Boot calcule los montos finales;
- la reserva creada aparezca en Mis reservas;
- el cliente pueda consultar su detalle;
- la cancelacion propia respete las reglas del backend;
- ningun dato de autorizacion venga del navegador;
- no exista acceso SQL directo desde MVC;
- no existan datos ficticios.
