# Contrato definitivo

El contrato REST objetivo aprobado para FASE 7.6 se encuentra en:

docs/REST_CONTRACT.md

API.md conserva tambien el inventario y contexto de los endpoints tecnicos existentes.

---

# API REST - Bembos Events

## Estado del documento

Este documento refleja el estado actual de la API antes de la auditoria definitiva de FASE 7.6.

Clasificacion utilizada:

- [IMPLEMENTADO] Endpoint existente que forma parte del sistema actual.
- [TECNICO] Endpoint creado para probar una regla de negocio.
- [REVISAR] Endpoint existente cuya forma final debe definirse.
- [PENDIENTE] Endpoint todavia no implementado.
- [FUTURO] Funcionalidad prevista para fases posteriores.

Base URL local:

```text
http://localhost:8080
```

# 1. Autenticacion

## [IMPLEMENTADO] Registro publico

```http
POST /api/auth/registro
```

Objetivo:

- registrar un nuevo usuario.
- siempre asignar rol CLIENTE.

Reglas:

- el usuario no puede elegir rol.
- el usuario no puede asignarse sede.
- el password se almacena con BCrypt.
- se crea un perfil Cliente activo asociado al Usuario.

Ejemplo:

```json
{
  "nombres": "Juan",
  "apellidos": "Perez",
  "correo": "juan@example.com",
  "telefono": "999999999",
  "password": "Password123!"
}
```

## [IMPLEMENTADO] Login

```http
POST /api/auth/login
```

Objetivo:

- validar correo y password.
- devolver JWT cuando las credenciales son correctas.

Ejemplo:

```json
{
  "correo": "juan@example.com",
  "password": "Password123!"
}
```

# 2. Usuarios administrativos

Acceso:

- ADMINISTRADOR.

## [IMPLEMENTADO] Crear usuario

```http
POST /api/usuarios
```

Permite crear usuarios administrativos o clientes desde BackOffice.

Para ENCARGADO_LOCAL se requiere una sede.

## [IMPLEMENTADO] Listar usuarios

```http
GET /api/usuarios
```

## [IMPLEMENTADO] Consultar usuario

```http
GET /api/usuarios/{idUsuario}
```

## [IMPLEMENTADO] Actualizar usuario

```http
PUT /api/usuarios/{idUsuario}
```

## [IMPLEMENTADO] Cambiar rol

```http
PUT /api/usuarios/{idUsuario}/rol
```

## [IMPLEMENTADO] Cambiar estado

```http
PATCH /api/usuarios/{idUsuario}/estado
```

# 3. BackOffice - Sedes

Acceso:

- ADMINISTRADOR.
- ENCARGADO_LOCAL.

## [IMPLEMENTADO] Listar sedes accesibles

```http
GET /api/backoffice/sedes
```

Comportamiento:

ADMINISTRADOR:

- obtiene todas las sedes activas accesibles.

ENCARGADO_LOCAL:

- obtiene solamente su sede asignada.

CLIENTE:

- recibe 403 Forbidden.

## [IMPLEMENTADO] Consultar sede

```http
GET /api/backoffice/sedes/{idSede}
```

Regla:

- ADMINISTRADOR puede consultar cualquier sede autorizada.
- ENCARGADO_LOCAL solamente puede consultar su sede.
- intentar acceder a otra sede devuelve 403.

# 4. Reservas - API tecnica actual

Los siguientes endpoints existen actualmente para probar reglas del dominio.

No representan necesariamente la API final que utilizara ASP.NET MVC.

Politica de seguridad actual:

- CLIENTE.
- ENCARGADO_LOCAL.
- ADMINISTRADOR.

segun la configuracion de seguridad vigente.

## [TECNICO] Validar capacidad

```http
POST /api/reservas/validar-capacidad
```

Ejemplo:

```json
{
  "idSalon": 1,
  "cantidadInvitados": 40
}
```

Regla:

```text
cantidadInvitados <= capacidadSalon
```

## [TECNICO] Validar disponibilidad

```http
POST /api/reservas/validar-disponibilidad
```

Ejemplo:

```json
{
  "idSalon": 1,
  "fechaEvento": "2026-12-20",
  "horaInicio": "14:00",
  "horaFin": "18:00"
}
```

Comprueba conflictos de horario.

## [TECNICO] Validar monto minimo

```http
POST /api/reservas/validar-monto-minimo
```

## [TECNICO] Calcular monto total

```http
POST /api/reservas/calcular-monto-total
```

## [TECNICO] Calcular deposito

```http
POST /api/reservas/calcular-deposito
```

## [TECNICO] Validar transicion de reserva

```http
POST /api/reservas/validar-transicion
```

Ejemplo:

```json
{
  "estadoActual": "PENDIENTE",
  "nuevoEstado": "CONFIRMADA"
}
```

Objetivo actual:

- comprobar las reglas de transicion.

Objetivo final:

- la validacion debe ejecutarse internamente cuando se modifique realmente una reserva.

# 5. Reservas - API objetivo

Los nombres exactos seran confirmados durante FASE 7.6.

## [IMPLEMENTADO] Crear reserva

Propuesta:

```http
POST /api/reservas
```

El backend debera validar dentro de una sola operacion:

- cliente.
- sede.
- salon.
- capacidad.
- horario.
- superposicion.
- paquete.
- tematica.
- servicios adicionales.
- precios.
- monto minimo.
- deposito.

## [IMPLEMENTADO] Mis reservas

Propuesta:

```http
GET /api/reservas/mias
```

Solo devuelve reservas pertenecientes al cliente autenticado.

Reglas:

- requiere rol CLIENTE.
- el cliente se obtiene a partir del correo autenticado.
- el navegador no envia idCliente.
- solamente se consultan reservas asociadas al Cliente autenticado.
- las reservas se ordenan por fechaCreacion descendente.
- si el cliente no tiene reservas responde 200 con una lista vacia.

## [IMPLEMENTADO - CLIENTE] Consultar reserva

```http
GET /api/reservas/{idReserva}
```

Estado actual:

- CLIENTE propietario: IMPLEMENTADO en FASE 7.6D.4.
- ENCARGADO_LOCAL: PENDIENTE para FASE 7.6E.
- ADMINISTRADOR: PENDIENTE para FASE 7.6E.

Para CLIENTE:

- requiere autenticacion con rol CLIENTE.
- el Cliente se obtiene desde el correo autenticado.
- el frontend no envia idCliente.
- la consulta utiliza conjuntamente idReserva e idCliente.
- solamente puede consultar una reserva propia.
- una reserva inexistente responde como recurso no encontrado.
- una reserva perteneciente a otro cliente responde de la misma forma para no revelar su existencia.
- la respuesta utiliza ReservaDetalleResponse y no expone directamente la entidad JPA.
## [IMPLEMENTADO - CLIENTE] Cancelar reserva propia

```http
POST /api/reservas/{idReserva}/cancelacion
```

Acceso:

- CLIENTE propietario.

Reglas:

- el Cliente se obtiene desde el correo autenticado.
- el frontend no envia idCliente.
- la reserva se busca mediante idReserva + idCliente.
- una reserva inexistente o perteneciente a otro cliente responde 404.
- PENDIENTE puede pasar a CANCELADA.
- CONFIRMADA puede pasar a CANCELADA.
- EN_PROCESO, COMPLETADA, CANCELADA y EXPIRADA no pueden cancelarse por esta operacion.
- una transicion no permitida responde 409 Conflict.
- actualiza estado y fechaActualizacion.
- registra HistorialReserva con el estado anterior, CANCELADA, fecha, observacion y Usuario responsable.
- la operacion no modifica automaticamente pagos ni realiza reembolsos.

# 6. Reservas BackOffice

Acceso:

- ADMINISTRADOR.
- ENCARGADO_LOCAL.

CLIENTE no tiene acceso a estos endpoints.

## [IMPLEMENTADO] Listar reservas BackOffice

```http
GET /api/backoffice/reservas
```

ADMINISTRADOR:

- obtiene todas las reservas.
- el resultado se ordena por fechaCreacion descendente.

ENCARGADO_LOCAL:

- la sede se obtiene mediante UsuarioSede y el correo autenticado.
- obtiene solamente las reservas pertenecientes a su sede.
- el navegador no envia idSede como fuente de autorizacion.

La respuesta utiliza ReservaResumenResponse.

Cuando no existen reservas accesibles se devuelve:

```text
200 OK
[]
```

## [IMPLEMENTADO] Consultar reserva BackOffice

```http
GET /api/backoffice/reservas/{idReserva}
```

ADMINISTRADOR:

- puede consultar cualquier reserva existente.

ENCARGADO_LOCAL:

- la busqueda utiliza idReserva + idSede asignada.
- no utiliza findById(idReserva) como mecanismo de autorizacion.
- una reserva perteneciente a otra sede responde 404.
- una reserva inexistente responde 404.

La respuesta utiliza ReservaDetalleResponse.

## [IMPLEMENTADO] Cambiar estado de reserva BackOffice

```http
PATCH /api/backoffice/reservas/{idReserva}/estado
```

Request:

```json
{
  "nuevoEstado": "CONFIRMADA"
}
```

Reglas:

- solamente se recibe nuevoEstado.
- el frontend no envia estadoActual.
- el estado actual se obtiene desde la Reserva persistida.
- ReservaService valida la transicion.
- una transicion no permitida responde 409 Conflict.
- ADMINISTRADOR dispone de alcance global.
- ENCARGADO_LOCAL solamente puede modificar reservas de su sede.
- una reserva de otra sede responde 404 para ENCARGADO_LOCAL.
- la modificacion utiliza PESSIMISTIC_WRITE.
- se actualiza fechaActualizacion.
- se registra HistorialReserva.
- el historial conserva estadoAnterior y estadoNuevo.
- el Usuario autenticado queda registrado como responsable.

Ejemplo de cambio valido:

```text
PENDIENTE -> CONFIRMADA
```

Las transiciones definitivas continúan siendo responsabilidad de ReglasTransicionReserva.

# 7. Pagos - API tecnica actual

Politica de seguridad actual:

- ENCARGADO_LOCAL.
- ADMINISTRADOR.

## [TECNICO] Validar transicion de pago

```http
POST /api/pagos/validar-transicion
```

Ejemplo:

```json
{
  "estadoActual": "PENDIENTE",
  "nuevoEstado": "APROBADO"
}
```

Este endpoint NO modifica ningun pago.

Solamente valida si la transicion seria permitida.

Debe considerarse temporal.

# 8. Pagos - API objetivo

## [PENDIENTE] Registrar pago

Propuesta:

```http
POST /api/pagos
```

## [PENDIENTE] Pagos de una reserva

Propuesta:

```http
GET /api/reservas/{idReserva}/pagos
```

## [PENDIENTE] Consultar pago

Propuesta:

```http
GET /api/pagos/{idPago}
```

## [PENDIENTE] Cambiar estado de pago

Propuesta:

```http
PATCH /api/pagos/{idPago}/estado
```

La operacion debera:

1. buscar pago.
2. buscar reserva relacionada.
3. validar rol.
4. validar sede.
5. validar transicion.
6. actualizar pago.
7. registrar acciones relacionadas.

Cuando este endpoint exista, validar-transicion dejara de ser necesario como endpoint publico.

# 9. Catalogos

## [PENDIENTE / REVISAR] Sedes publicas

Debe existir una API publica adecuada para que el cliente consulte sedes disponibles.

## [PENDIENTE / REVISAR] Salones

Debe permitir consultar salones por sede.

Propuesta:

```http
GET /api/sedes/{idSede}/salones
```

## [PENDIENTE / REVISAR] Horarios

Debe permitir consultar disponibilidad y horarios de salon.

## [PENDIENTE / REVISAR] Paquetes

Debe permitir consultar paquetes activos.

## [PENDIENTE / REVISAR] Tematicas

Debe permitir consultar tematicas activas.

## [PENDIENTE / REVISAR] Servicios adicionales

Debe permitir consultar servicios adicionales activos.

# 10. MFA - futuro

Endpoints candidatos:

```text
POST   /api/auth/mfa/setup
POST   /api/auth/mfa/activar
POST   /api/auth/mfa/verificar
DELETE /api/auth/mfa
```

Estos endpoints NO existen actualmente.

# 11. Recuperacion de password - futuro

Endpoints candidatos:

```text
POST /api/auth/password/solicitar
POST /api/auth/password/restablecer
```

# 12. Chat - futuro

API exacta pendiente de diseno.

Debera soportar:

- iniciar conversacion.
- enviar mensaje.
- historial.
- escalar a humano.
- asignar encargado.
- cerrar conversacion.

# 13. Errores HTTP

Convenciones objetivo:

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
```

FASE 7.6 definira cuando corresponde utilizar cada codigo.

# 14. Regla fundamental

El frontend nunca debe hacer una validacion previa y asumir que la operacion posterior sera valida.

Ejemplo incorrecto:

```text
Frontend
  -> validar-transicion
  -> recibe OK
  -> modificar estado
```

Flujo correcto:

```text
Frontend
   |
   v
solicita cambio real
   |
   v
Backend
   |
   +-- valida permisos
   +-- valida sede
   +-- valida transicion
   +-- modifica
   +-- persiste
```

# 15. Objetivo de FASE 7.6

Cada endpoint actual sera clasificado como:

- DEFINITIVO.
- TECNICO.
- REEMPLAZAR.
- ELIMINAR.
- PENDIENTE.

El resultado sera el contrato REST definitivo antes de desarrollar el frontend ASP.NET Core MVC.


## FASE 7.6C.1 - Catalogos publicos implementados

Disponibles sin autenticacion:

- GET /api/sedes
- GET /api/sedes/{idSede}
- GET /api/sedes/{idSede}/salones
- GET /api/salones/{idSalon}
- GET /api/salones/{idSalon}/horarios
- GET /api/paquetes
- GET /api/paquetes/{idPaquete}
- GET /api/tematicas
- GET /api/tematicas/{idTematica}
- GET /api/servicios-adicionales
- GET /api/servicios-adicionales/{idServicioAdicional}

Solo se exponen recursos activos.

La disponibilidad del salon se implementa en FASE 7.6C.2.


## FASE 7.6C.2 - Disponibilidad publica

```http
GET /api/salones/{idSalon}/disponibilidad?fecha=2026-10-19&horaInicio=14:00&horaFin=18:00
```

La operacion:

- exige salon activo.
- exige sede activa.
- valida el horario configurado del salon.
- reutiliza ReservaService para detectar solapamientos.
- no crea ni modifica reservas.

Ejemplo disponible:

```json
{
  "idSalon": 1,
  "fecha": "2026-10-19",
  "horaInicio": "14:00:00",
  "horaFin": "18:00:00",
  "disponible": true,
  "motivo": "Disponible"
}
```
