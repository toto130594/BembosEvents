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

# 6. Endpoints tecnicos actuales de Reserva

| Metodo | Endpoint | Estado final |
|---|---|---|
| POST | /api/reservas/validar-capacidad | RETIRAR |
| POST | /api/reservas/validar-disponibilidad | RETIRAR |
| POST | /api/reservas/validar-monto-minimo | RETIRAR |
| POST | /api/reservas/calcular-monto-total | RETIRAR |
| POST | /api/reservas/calcular-deposito | RETIRAR |
| POST | /api/reservas/validar-transicion | RETIRAR |

Las reglas internas NO se eliminan.

Se reutilizaran desde ReservaService.

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

El acceso de ENCARGADO_LOCAL y ADMINISTRADOR se implementara en FASE 7.6E.

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
| GET | /api/backoffice/reservas | ADMINISTRADOR / ENCARGADO_LOCAL | IMPLEMENTAR |
| GET | /api/backoffice/reservas/{idReserva} | ADMINISTRADOR / ENCARGADO_LOCAL | IMPLEMENTAR |
| PATCH | /api/backoffice/reservas/{idReserva}/estado | ADMINISTRADOR / ENCARGADO_LOCAL | IMPLEMENTAR |

## Regla de sede

ADMINISTRADOR:

```text
todas las sedes
```

ENCARGADO_LOCAL:

```text
UsuarioSede -> sede asignada -> reservas de esa sede
```

Un parametro enviado por el navegador nunca puede ampliar el alcance del encargado.

# 10. Cambio de estado de Reserva

Flujo:

```text
buscar reserva
      |
      v
validar acceso
      |
      v
validar sede
      |
      v
validar transicion
      |
      v
actualizar estado
      |
      v
registrar historial
      |
      v
guardar
```

# 11. Endpoints tecnicos actuales de Pago

| Metodo | Endpoint | Estado final |
|---|---|---|
| POST | /api/pagos/validar-transicion | RETIRAR |

PagoService.validarTransicionEstado se conserva como regla interna.

# 12. Pagos - contrato base

| Metodo | Endpoint | Acceso | Estado |
|---|---|---|---|
| POST | /api/reservas/{idReserva}/pagos | CLIENTE propietario | IMPLEMENTAR |
| GET | /api/reservas/{idReserva}/pagos | CLIENTE propietario | IMPLEMENTAR |
| GET | /api/pagos/{idPago} | CLIENTE propietario | IMPLEMENTAR |
| GET | /api/backoffice/pagos | ADMINISTRADOR / ENCARGADO_LOCAL | IMPLEMENTAR |
| GET | /api/backoffice/pagos/{idPago} | ADMINISTRADOR / ENCARGADO_LOCAL | IMPLEMENTAR |

## Inicio de pago

```http
POST /api/reservas/{idReserva}/pagos
```

La API sera independiente del proveedor de pago.

PagoService sera responsable de iniciar el proceso.

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
