# Contrato REST Definitivo - Bembos Events

## Estado

FASE 7.6B.

Este documento define el contrato REST objetivo que debe existir antes de desarrollar el frontend ASP.NET Core MVC.

Clasificacion:

- MANTENER: endpoint existente que continuara.
- IMPLEMENTAR: endpoint necesario que aun no existe.
- RETIRAR: endpoint tecnico que dejara de formar parte de la API final.
- FUTURO: endpoint previsto para una fase posterior.

# 1. Principios

## Backend como autoridad

El backend decide definitivamente:

- permisos.
- alcance por sede.
- capacidad.
- disponibilidad.
- precios.
- monto minimo.
- deposito.
- transiciones.
- persistencia.

## Frontend

El frontend solicita operaciones.

No determina si una operacion es valida.

## IA

La IA utiliza este mismo backend y no obtiene privilegios especiales.

## Seguridad por sede

ADMINISTRADOR:

- alcance global.

ENCARGADO_LOCAL:

- solamente recursos de su sede.

CLIENTE:

- solamente recursos propios cuando corresponda.

# 2. Autenticacion

| Metodo | Endpoint | Acceso | Estado | Service |
|---|---|---|---|---|
| POST | /api/auth/registro | PUBLICO | MANTENER | AuthService |
| POST | /api/auth/login | PUBLICO | MANTENER | AuthService |

## Registro

El registro publico siempre crea CLIENTE.

No permite seleccionar rol.

El Usuario registrado como CLIENTE dispone de un perfil Cliente activo asociado.

# 3. Usuarios administrativos

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| POST | /api/usuarios | ADMINISTRADOR | MANTENER |
| GET | /api/usuarios | ADMINISTRADOR | MANTENER |
| GET | /api/usuarios/{idUsuario} | ADMINISTRADOR | MANTENER |
| PUT | /api/usuarios/{idUsuario} | ADMINISTRADOR | MANTENER |
| PUT | /api/usuarios/{idUsuario}/rol | ADMINISTRADOR | MANTENER |
| PATCH | /api/usuarios/{idUsuario}/estado | ADMINISTRADOR | MANTENER |

Service:

- UsuarioAdminService.

# 4. Catalogos publicos

Los catalogos publicos solamente devolveran registros activos.

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| GET | /api/sedes | PUBLICO | MANTENER |
| GET | /api/sedes/{idSede} | PUBLICO | MANTENER |
| GET | /api/sedes/{idSede}/salones | PUBLICO | MANTENER |
| GET | /api/salones/{idSalon} | PUBLICO | MANTENER |
| GET | /api/salones/{idSalon}/horarios | PUBLICO | MANTENER |
| GET | /api/salones/{idSalon}/disponibilidad | PUBLICO | MANTENER |
| GET | /api/paquetes | PUBLICO | MANTENER |
| GET | /api/paquetes/{idPaquete} | PUBLICO | MANTENER |
| GET | /api/tematicas | PUBLICO | MANTENER |
| GET | /api/tematicas/{idTematica} | PUBLICO | MANTENER |
| GET | /api/servicios-adicionales | PUBLICO | MANTENER |
| GET | /api/servicios-adicionales/{idServicio} | PUBLICO | MANTENER |

# 5. Disponibilidad

Endpoint:

```http
GET /api/salones/{idSalon}/disponibilidad
```

Parametros previstos:

- fecha.
- horaInicio.
- horaFin.

Respuesta conceptual:

```json
{
  "disponible": true
}
```

La consulta de disponibilidad sirve para informar al usuario.

Al crear una reserva el backend DEBE volver a validar disponibilidad.

# 6. Reglas internas de Reserva

Los endpoints tecnicos independientes de validacion y calculo fueron retirados en FASE 7.6G.

Las reglas de negocio NO fueron eliminadas.

Continuan disponibles internamente mediante `ReservaService`:

- `validarCapacidad`.
- `validarDisponibilidad`.
- `validarMontoMinimo`.
- `calcularMontoTotal`.
- `calcularMontoDeposito`.
- `validarTransicionEstado`.

Estas reglas son utilizadas por los flujos REST definitivos de Reserva, disponibilidad y BackOffice.

El frontend no debe ejecutar estas reglas mediante endpoints auxiliares separados.
# 7. Reserva - Cliente

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| POST | /api/reservas | CLIENTE | MANTENER |
| GET | /api/reservas/mias | CLIENTE | MANTENER |
| GET | /api/reservas/{idReserva} | CLIENTE propietario | MANTENER |
| POST | /api/reservas/{idReserva}/cancelacion | CLIENTE propietario | MANTENER |

## Crear reserva

```http
POST /api/reservas
```

El request podra contener:

- idSede.
- idSalon.
- idPaquete.
- idTematica.
- fechaEvento.
- horaInicio.
- horaFin.
- cantidadInvitados.
- servicios adicionales.

El cliente NO enviara como fuente de verdad:

- montoBase.
- montoServicios.
- montoTotal.
- porcentajeDeposito.
- montoDeposito.
- estado definitivo.

ReservaService calculara esos valores.

## Flujo de creacion

```text
POST /api/reservas
        |
        v
identificar CLIENTE
        |
        v
validar sede
        |
        v
validar salon
        |
        v
validar capacidad
        |
        v
validar disponibilidad
        |
        v
validar catalogos
        |
        v
calcular precio
        |
        v
validar monto minimo
        |
        v
calcular deposito
        |
        v
generar codigo
        |
        v
guardar Reserva
        |
        v
guardar ReservaServicio
        |
        v
guardar HistorialReserva
```

## Mis reservas del cliente

GET /api/reservas/mias

Acceso:

- CLIENTE autenticado.

El backend identifica al Cliente utilizando el correo del usuario autenticado.

El frontend no puede indicar ni sustituir idCliente.

La consulta:

- filtra exclusivamente por Cliente.idCliente.
- no utiliza una consulta global de reservas.
- ordena por fechaCreacion descendente.
- responde 200 con una lista vacia cuando no existen reservas.

La respuesta utiliza ReservaResumenResponse y no expone directamente la entidad JPA.

## Consultar reserva propia - CLIENTE

```http
GET /api/reservas/{idReserva}
```

Acceso:

- CLIENTE propietario.

Reglas:

- el Cliente se obtiene desde el correo autenticado.
- el frontend no envia idCliente.
- la busqueda utiliza idReserva + idCliente.
- no se utiliza findById(idReserva) como mecanismo de autorizacion.
- una reserva inexistente responde como recurso no encontrado.
- una reserva de otro cliente responde de la misma forma para no revelar su existencia.

Respuesta:

- utiliza ReservaDetalleResponse.
- incluye datos generales de la reserva.
- incluye montos calculados al momento de crear la reserva.
- incluye servicios adicionales.
- los servicios conservan cantidad, precioUnitario historico y subtotal historico.

El acceso de ENCARGADO_LOCAL y ADMINISTRADOR se encuentra implementado mediante los endpoints BackOffice.

# 8. Cancelacion por cliente

El cliente NO dispone de un endpoint generico para establecer cualquier estado.

Utiliza una accion especifica:

```http
POST /api/reservas/{idReserva}/cancelacion
```

Acceso:

- CLIENTE propietario.

Seguridad:

- el Cliente se obtiene desde el correo autenticado.
- el frontend no envia idCliente.
- la reserva se busca mediante idReserva + idCliente.
- una reserva inexistente y una reserva ajena responden 404 para no revelar informacion.

Transiciones permitidas:

- PENDIENTE -> CANCELADA.
- CONFIRMADA -> CANCELADA.

No se permite cancelar desde:

- EN_PROCESO.
- COMPLETADA.
- CANCELADA.
- EXPIRADA.

ReservaService valida la transicion antes de modificar la reserva.

Una transicion no permitida responde 409 Conflict.

Cuando la cancelacion es valida:

- estado pasa a CANCELADA.
- se actualiza fechaActualizacion.
- se registra HistorialReserva.
- el historial conserva estadoAnterior y estadoNuevo.
- la observacion identifica que la cancelacion fue realizada por el cliente.
- UsuarioResponsable corresponde al Usuario asociado al Cliente autenticado.

Esta operacion no cambia automaticamente el estado de los pagos ni ejecuta reembolsos.
# 9. Reserva - BackOffice

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| GET | /api/backoffice/reservas | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |
| GET | /api/backoffice/reservas/{idReserva} | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |
| PATCH | /api/backoffice/reservas/{idReserva}/estado | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |

## Regla de alcance

ADMINISTRADOR:

- dispone de alcance global.
- puede listar reservas de todas las sedes.
- puede consultar reservas de cualquier sede.
- puede cambiar el estado de reservas de cualquier sede.

ENCARGADO_LOCAL:

- obtiene su sede desde UsuarioSede utilizando el correo autenticado.
- solamente lista reservas de su sede asignada.
- solamente consulta reservas de su sede asignada.
- solamente puede cambiar el estado de reservas de su sede asignada.
- el frontend no puede indicar una sede diferente para ampliar su alcance.

CLIENTE:

- no tiene acceso a los endpoints de Reservas BackOffice.

## Listado BackOffice

```http
GET /api/backoffice/reservas
```

Las reservas se ordenan por fechaCreacion descendente.

ADMINISTRADOR utiliza una consulta global.

ENCARGADO_LOCAL utiliza exclusivamente el idSede obtenido desde UsuarioSede.

El endpoint no recibe idSede como parametro de autorizacion.

## Detalle BackOffice

```http
GET /api/backoffice/reservas/{idReserva}
```

ADMINISTRADOR puede consultar cualquier reserva existente.

Para ENCARGADO_LOCAL la consulta utiliza conjuntamente:

- idReserva.
- idSede asignada.

Una reserva inexistente o una reserva perteneciente a otra sede responde 404 Not Found.

No se utiliza findById(idReserva) como mecanismo de autorizacion para ENCARGADO_LOCAL.

# 10. Cambio de estado de Reserva

Endpoint:

```http
PATCH /api/backoffice/reservas/{idReserva}/estado
```

Acceso:

- ADMINISTRADOR.
- ENCARGADO_LOCAL.

Request:

```json
{
  "nuevoEstado": "CONFIRMADA"
}
```

El frontend solamente envia nuevoEstado.

El frontend no envia estadoActual.

El estado actual se obtiene directamente desde la Reserva persistida.

Flujo:

```text
buscar reserva con bloqueo
      |
      v
validar alcance administrativo
      |
      v
validar sede
      |
      v
obtener estado actual
      |
      v
validar transicion
      |
      v
actualizar estado
      |
      v
actualizar fechaActualizacion
      |
      v
registrar HistorialReserva
      |
      v
registrar Usuario responsable
      |
      v
persistir
```

La validacion de transicion reutiliza:

```text
ReservaService.validarTransicionEstado(...)
```

Las transiciones permitidas continúan siendo las definidas por ReglasTransicionReserva.

Una transicion no permitida responde:

```text
409 Conflict
```

La operacion de modificacion utiliza PESSIMISTIC_WRITE para proteger el estado de la reserva frente a modificaciones concurrentes.

ADMINISTRADOR utiliza una consulta bloqueada por idReserva.

ENCARGADO_LOCAL utiliza una consulta bloqueada por idReserva + idSede.

Una reserva perteneciente a otra sede no queda visible para ENCARGADO_LOCAL y responde 404.

Los endpoints GET de reservas BackOffice no utilizan bloqueo de escritura.

# 11. Reglas internas de Pago

El endpoint tecnico de validacion de transiciones fue retirado en FASE 7.6G.

La regla interna:

`PagoService.validarTransicionEstado`

se conserva para ser utilizada por flujos reales del backend cuando corresponda.

El frontend no dispone de un endpoint auxiliar para decidir o validar de forma independiente el estado de un Pago.
# 12. Pagos - contrato base

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| POST | /api/reservas/{idReserva}/pagos | CLIENTE propietario | MANTENER |
| GET | /api/reservas/{idReserva}/pagos | CLIENTE propietario | MANTENER |
| GET | /api/pagos/{idPago} | CLIENTE propietario | MANTENER |
| GET | /api/backoffice/pagos | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |
| GET | /api/backoffice/pagos/{idPago} | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |

## Inicio de pago

```http
POST /api/reservas/{idReserva}/pagos
```

La API sera independiente del proveedor de pago.

PagoService sera responsable de iniciar el proceso.
## Reglas implementadas en FASE 7.6F

### Cliente

El Cliente se obtiene desde el correo autenticado.

El frontend no envia `idCliente`.

Para iniciar un pago:

```http
POST /api/reservas/{idReserva}/pagos
```

El backend valida que la Reserva pertenezca al Cliente autenticado.

Una Reserva inexistente o perteneciente a otro Cliente responde como recurso no encontrado.

El request contiene exclusivamente los datos necesarios para iniciar el pago:

```json
{
  "monto": 250.00,
  "metodoPago": "PRUEBA"
}
```

Reglas:

- `monto` debe ser mayor que 0.
- se admiten pagos parciales.
- una Reserva puede tener multiples Pagos.
- `metodoPago` es obligatorio y no supera 50 caracteres.
- 7.6F no define todavia un catalogo cerrado de metodos de pago.
- el estado inicial es `PENDIENTE`.
- `referenciaExterna` inicia en `null`.
- `fechaPago` inicia en `null`.
- `fechaCreacion` es generada por el backend.
- el Cliente no decide el estado inicial del pago.
- el Cliente no envia `referenciaExterna`.
- el Cliente no envia `fechaPago`.

Los pagos de una Reserva propia se consultan mediante:

```http
GET /api/reservas/{idReserva}/pagos
```

La respuesta se ordena por `fechaCreacion` descendente.

Si la Reserva no tiene Pagos se devuelve `200` con una lista vacia.

El detalle de un Pago propio se consulta mediante:

```http
GET /api/pagos/{idPago}
```

La consulta utiliza conjuntamente `idPago` y el Cliente autenticado.

Un Pago inexistente o perteneciente a otro Cliente responde como recurso no encontrado.

### BackOffice

```http
GET /api/backoffice/pagos
GET /api/backoffice/pagos/{idPago}
```

ADMINISTRADOR:

- dispone de alcance global.
- puede consultar todos los Pagos.

ENCARGADO_LOCAL:

- la sede se obtiene a partir del usuario autenticado.
- no recibe `idSede` desde el frontend como fuente de autorizacion.
- solamente consulta Pagos asociados a Reservas de su sede.
- el detalle utiliza conjuntamente `idPago` e `idSede`.
- un Pago de otra sede responde como recurso no encontrado.

Los listados se ordenan por `fechaCreacion` descendente.

### Estados

7.6F no expone un endpoint definitivo que permita al navegador aprobar o rechazar Pagos.

Desde FASE 7.6G no existe un endpoint tecnico de validacion de transiciones de Pago.

La regla interna `PagoService.validarTransicionEstado` se conserva.

La aprobacion definitiva de un Pago quedara vinculada al flujo seguro del proveedor en FASE 11.

# 13. Cambio de estado de Pago

No existira en la API final un endpoint generico que permita al frontend decidir libremente:

```text
PENDIENTE -> APROBADO
```

La aprobacion definitiva debera originarse en el flujo seguro del proveedor de pagos.

PagoService conservara la regla interna validarTransicionEstado.

# 14. Mercado Pago - futuro

Estado:

FUTURO - FASE 11.

Flujo objetivo:

```text
CLIENTE
   |
   v
iniciar pago
   |
   v
Proveedor
   |
   v
Webhook
   |
   v
validar autenticidad
   |
   v
validar idempotencia
   |
   v
PagoService
   |
   v
actualizar estado
```

Endpoint futuro de webhook:

```http
POST /api/webhooks/pagos/mercado-pago
```

# 15. BackOffice Sedes

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| GET | /api/backoffice/sedes | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |
| GET | /api/backoffice/sedes/{idSede} | ADMINISTRADOR / ENCARGADO_LOCAL | MANTENER |

## Regla definitiva de alcance para Sedes

ADMINISTRADOR:

- puede listar todas las sedes activas.
- puede consultar cualquier sede activa.

ENCARGADO_LOCAL:

- obtiene su sede exclusivamente desde `UsuarioSede` usando la identidad autenticada.
- al listar sedes solamente recibe su sede asignada.
- solamente puede consultar el detalle de su sede asignada.
- una sede diferente a la asignada responde 404.
- una sede inexistente o inactiva responde 404.
- el `idSede` recibido por URL nunca amplia el alcance autorizado.

CLIENTE:

- no tiene acceso a `/api/backoffice/**`.

La ruta `/api/backoffice/**` tambien esta protegida en `SecurityConfig` para `ADMINISTRADOR` y `ENCARGADO_LOCAL` como defensa en profundidad.
# 16. Administracion de catalogos

La administracion CRUD de sedes, salones, paquetes, tematicas y servicios adicionales no forma parte de 7.6C.

Se incorporara posteriormente cuando el BackOffice requiera mantenimiento funcional de catalogos.

La API publica de lectura SI forma parte de FASE 7.6.

# 17. Codigos HTTP

Convencion:

| Codigo | Uso |
|---|---|
| 200 | Consulta o actualizacion correcta |
| 201 | Recurso creado |
| 204 | Operacion correcta sin cuerpo |
| 400 | Request invalido |
| 401 | No autenticado |
| 403 | Autenticado pero sin permiso |
| 404 | Recurso inexistente o no visible |
| 409 | Conflicto de negocio |

# 18. Regla para recursos ajenos

Cuando corresponda evitar revelar la existencia de recursos de otro cliente se podra responder 404 en lugar de exponer informacion mediante 403.

# 19. Orden de implementacion

## FASE 7.6C

Catalogos publicos.

## FASE 7.6D

Reservas del cliente.

## FASE 7.6E

Reservas BackOffice.

## FASE 7.6F

Pagos base.

## FASE 7.6G

Retirar endpoints tecnicos.

## FASE 7.6H

Documentacion, Postman y tests.

## FASE 7.6I

Validacion final y runtime smoke test.

# 20. Definition of Done REST

Un endpoint no esta terminado hasta comprobar:

- Controller.
- DTO.
- Service.
- Repository si corresponde.
- seguridad.
- acceso por sede si corresponde.
- tests.
- API.md.
- REST_CONTRACT.md.
- Postman.
- README si cambia el flujo general.
- CHANGELOG.
- validate-backend.ps1.
