# Frontend BackOffice Flow

## 1. Objetivo

La fase 8.0.11 implementa el BackOffice MVC de Bembos Events.

Arquitectura:

Browser -> ASP.NET Core MVC -> BackendApiClient -> Spring Boot -> Service -> Repository / JPA -> SQL Server.

El frontend nunca accede directamente a SQL Server.
El JWT permanece server-side.
Spring Boot conserva la autoridad final.

## 2. Roles

Roles permitidos:

- ADMINISTRADOR
- ENCARGADO_LOCAL

CLIENTE no pertenece al BackOffice.

ADMINISTRADOR posee alcance global.

ENCARGADO_LOCAL posee alcance limitado a su sede.

La sede autorizada es obtenida por Spring desde Authentication y UsuarioSede.
El browser nunca envia un idSede para concederse autorizacion.

## 3. Seguridad MVC

Todas las rutas /backoffice requieren autenticacion.

Semantica:

- 401: limpiar sesion backend y volver a login.
- 403: AccessDenied.
- 404: NotFound sin enumerar recursos.
- 400: error de validacion.
- 409: conflicto de negocio.
- 5xx: mensaje generico.

Spring Security sigue siendo la autoridad final.

## 4. Sedes BackOffice

REST:

- GET /api/backoffice/sedes
- GET /api/backoffice/sedes/{idSede}

MVC:

- GET /backoffice/sedes
- GET /backoffice/sedes/{idSede}

No se implementa CRUD de Sede porque el backend auditado no lo expone.

## 5. Reservas BackOffice

REST:

- GET /api/backoffice/reservas
- GET /api/backoffice/reservas/{idReserva}
- PATCH /api/backoffice/reservas/{idReserva}/estado

MVC:

- GET /backoffice/reservas
- GET /backoffice/reservas/{idReserva}
- POST /backoffice/reservas/{idReserva}/estado

El POST MVC usa antiforgery.
BackendApiClient realiza PATCH contra Spring.

El request solamente envia NuevoEstado.

El frontend NO envia:

- EstadoActual
- IdCliente
- IdUsuario
- IdSede para autorizar

Spring obtiene el estado actual desde la Reserva persistida.

## 6. Estados Reserva

- PENDIENTE
- CONFIRMADA
- EN_PROCESO
- COMPLETADA
- CANCELADA
- EXPIRADA

## 7. Transiciones

| Estado actual | Puede cambiar a |
| --- | --- |
| PENDIENTE | CONFIRMADA, CANCELADA, EXPIRADA |
| CONFIRMADA | EN_PROCESO, CANCELADA |
| EN_PROCESO | COMPLETADA |
| COMPLETADA | ninguno |
| CANCELADA | ninguno |
| EXPIRADA | ninguno |

Spring valida nuevamente toda transicion.

## 8. Pagos BackOffice

REST:

- GET /api/backoffice/pagos
- GET /api/backoffice/pagos/{idPago}

MVC:

- GET /backoffice/pagos
- GET /backoffice/pagos/{idPago}

Pagos BackOffice sera solo lectura.

No existe mutacion BackOffice de Pago en el contrato auditado.

## 9. Dashboard

MVC:

- GET /backoffice

Puede mostrar:

- numero de sedes accesibles;
- numero de reservas accesibles;
- numero de pagos accesibles;
- accesos directos.

No consulta SQL directamente.

## 10. Layout

Navegacion:

- Inicio
- Sedes
- Reservas
- Pagos
- Volver al portal
- Cerrar sesion

## 11. Scope

ADMINISTRADOR:

- alcance global segun Spring.

ENCARGADO_LOCAL:

- alcance de su sede segun Authentication.

Un id incluido en una URL sirve para navegar.
Nunca concede autorizacion.

## 12. Anti-enumeration

MVC respeta 404 de Spring.

No realizara consultas adicionales para descubrir si un recurso existe en otra sede.

Aplica a:

- Sedes
- Reservas
- Pagos

## 13. Datos DEV

Estado actual:

- Reserva 1 = CANCELADA
- Reserva 2 = PENDIENTE
- Pago 1 = PENDIENTE

Reserva 1 no se modifica.

Reserva 2 puede usarse inicialmente para pruebas de lectura.

La prueba real de cambio de estado se reserva para 8.0.11H.

## 14. Fuera de alcance

- CRUD de usuarios
- gestion completa de roles
- administracion de UsuarioSede
- mutacion administrativa de pagos
- Mercado Pago
- SQL desde frontend
- JWT en browser
- endpoints inventados

## 15. Roadmap

### 8.0.11A - Auditoria BackOffice

CERRADA.

### 8.0.11B - Diseno BackOffice MVC

Arquitectura, rutas, roles y seguridad.

### 8.0.11C - Auth roles + base tecnica

- auditar claims del JWT;
- bridge de roles server-side;
- DTOs;
- BackendApiClient;
- controller base;
- layout BackOffice.

### 8.0.11D - Dashboard + Sedes

- dashboard;
- listado sedes;
- detalle sede.

### 8.0.11E - Reservas listado + detalle

- listado;
- detalle;
- scope;
- anti-enumeration.

### 8.0.11F - Cambio estado Reserva

- POST MVC;
- antiforgery;
- PATCH Spring;
- request solo NuevoEstado;
- PRG;
- 400, 401, 403, 404, 409.

### 8.0.11G - Pagos listado + detalle

- listado;
- detalle;
- solo lectura.

### 8.0.11H - Smoke roles y scope

- ADMINISTRADOR;
- ENCARGADO_LOCAL;
- CLIENTE rechazado;
- recurso fuera de sede;
- cambio real controlado;
- Reserva 1 intacta.

### 8.0.11I - Cierre

- dotnet Release;
- backend validator;
- Docker frontend;
- regresion;
- seguridad;
- integridad BD;
- checkpoint commit;
- sin push automatico.

## 16. Principios

1. Browser -> MVC -> Spring -> JPA -> SQL.
2. Frontend no usa SQL directo.
3. JWT permanece server-side.
4. Spring decide autorizacion.
5. Spring decide scope por sede.
6. Browser no decide sede autorizada.
7. Browser no envia IdCliente.
8. Browser no envia EstadoActual.
9. CLIENTE no accede a BackOffice.
10. No inventar endpoints.
11. No inventar permisos.
12. No inventar reglas de negocio.
13. No realizar push automaticamente.
## 17. Decision tecnica 8.0.11C

La auditoria confirmo que el JWT actual no contiene claims de rol.

Por esa razon el MVC no inventara ClaimTypes.Role.

La proteccion BackOffice utiliza un capability gate server-side:

1. el usuario inicia sesion normalmente;
2. el JWT queda almacenado en Session;
3. al acceder a BackOffice, MVC ejecuta GET /api/backoffice/sedes;
4. BackendBearerHandler adjunta el JWT server-side;
5. Spring Security decide el acceso;
6. HTTP 200 permite continuar;
7. HTTP 403 envia a AccessDenied;
8. HTTP 401 limpia Session y cookie y vuelve al login.

Este mecanismo no intenta distinguir artificialmente entre
ADMINISTRADOR y ENCARGADO_LOCAL.

Spring sigue siendo responsable de:

- roles;
- scope por sede;
- acceso a cada recurso;
- anti-enumeration.

El capability gate solamente determina si la identidad autenticada
puede entrar al area BackOffice.

No se modifica el backend para duplicar informacion de roles en
AuthResponse ni dentro del JWT.
## 18. Cierre 8.0.11

La implementacion BackOffice MVC quedo cerrada con los siguientes modulos:

- dashboard;
- sedes;
- reservas;
- cambio administrativo de estado de reserva;
- pagos en modo consulta.

Seguridad validada:

- ADMINISTRADOR posee alcance global;
- ENCARGADO_LOCAL conserva scope por sede;
- CLIENTE recibe acceso denegado al BackOffice;
- recursos ajenos al ENCARGADO_LOCAL se anti-enumeran con 404;
- el JWT permanece exclusivamente server-side;
- el navegador no envia idSede de autorizacion;
- el navegador no envia estadoActual para una transicion;
- Spring Boot conserva la autoridad final.

Cambio de estado validado end-to-end:

- MVC POST protegido por antiforgery;
- idReserva proviene de la ruta;
- request REST contiene solamente nuevoEstado;
- Spring carga el estado persistido;
- Spring valida la transicion;
- PRG despues del cambio exitoso;
- HistorialReserva registra la transicion y usuario responsable.

Smoke final controlado:

- Reserva 1 permanece CANCELADA;
- Reserva 2 cambio una sola vez de PENDIENTE a CONFIRMADA;
- Pago 1 permanece PENDIENTE;
- no se modifico backend;
- no se modifico Flyway;
- frontend no accede directamente a SQL Server.

Pagos BackOffice permanecen de solo lectura porque el contrato backend actual
no expone una mutacion administrativa de pagos.

Mercado Pago permanece fuera del alcance de 8.0.11.
