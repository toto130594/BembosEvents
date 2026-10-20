# Decisiones Arquitectonicas

Este documento registra decisiones importantes del proyecto para evitar perder el contexto tecnico con el tiempo.

## ADR-001 - Java 21

El backend utiliza Java 21.

No se cambia la version sin una decision explicita.

## ADR-002 - Spring Boot

Spring Boot es el backend REST principal.

## ADR-003 - SQL Server

Microsoft SQL Server 2022 es la base de datos principal.

## ADR-004 - Flyway

Flyway es la fuente de verdad para la evolucion del esquema.

Cada cambio estructural nuevo requiere una nueva migracion.

## ADR-005 - ASP.NET Core MVC

El frontend sera desarrollado con ASP.NET Core MVC.

El frontend consumira Spring Boot mediante HTTP y JSON.

## ADR-006 - JWT

La autenticacion actual utiliza JWT stateless.

## ADR-007 - BCrypt

Los passwords se almacenan mediante BCrypt.

Nunca se almacenan passwords en texto plano.

## ADR-008 - Roles

Roles principales:

- CLIENTE.
- ENCARGADO_LOCAL.
- ADMINISTRADOR.

## ADR-009 - UsuarioSede

UsuarioSede representa el alcance territorial de ENCARGADO_LOCAL.

Conceptualmente:

```text
Rol = que puede hacer
UsuarioSede = donde puede hacerlo
```

## ADR-010 - Administrador global

ADMINISTRADOR puede operar sobre todas las sedes.

No necesita UsuarioSede para obtener alcance global.

## ADR-011 - Encargado limitado por sede

ENCARGADO_LOCAL solo puede operar sobre la sede asignada.

Esta restriccion debe aplicarse progresivamente a todos los recursos de BackOffice.

## ADR-012 - Backend como fuente de verdad

El frontend no decide definitivamente:

- disponibilidad.
- capacidad.
- precios.
- permisos.
- pagos.
- transiciones de estado.

## ADR-013 - IA sin acceso directo a base de datos

La IA nunca tendra acceso directo a SQL Server.

Debe utilizar APIs o Services controlados.

## ADR-014 - Chat hibrido

El chat futuro comenzara con IA y podra escalar a un encargado humano.

La conversacion debe conservarse durante el escalamiento.

## ADR-015 - Chat asociado a sede

Cuando la conversacion tenga una sede seleccionada, el escalamiento humano debe dirigirse solamente a encargados autorizados para esa sede.

## ADR-016 - MFA mediante TOTP

El mecanismo previsto para MFA sera TOTP.

Politica objetivo:

- CLIENTE: opcional.
- ENCARGADO_LOCAL: obligatorio.
- ADMINISTRADOR: obligatorio.

Tambien se implementaran codigos de recuperacion.

## ADR-017 - Pagos idempotentes

La integracion futura con Mercado Pago debe soportar idempotencia.

Un mismo evento del proveedor no debe procesarse dos veces.

## ADR-018 - Webhooks seguros

Los webhooks futuros deben validar autenticidad o firma antes de modificar datos.

## ADR-019 - Eventos internos

Se evaluara utilizar eventos internos para desacoplar acciones secundarias.

Ejemplos:

- notificaciones.
- auditoria.
- historial.
- correo.
- chat.

## ADR-020 - Documentacion obligatoria

Codigo y documentacion forman parte de la misma entrega.

Una funcionalidad con documentacion desactualizada se considera incompleta.

## ADR-021 - Endpoints tecnicos temporales

Los endpoints de validacion creados durante las primeras fases sirven para comprobar reglas de negocio.

No todos deben permanecer en la API final.

FASE 7.6 revisara cada endpoint y lo clasificara como:

- definitivo.
- temporal.
- reemplazar.
- eliminar.
- pendiente de implementar.


## 2026-10-16 - Perfil Cliente persistente asociado al rol CLIENTE

Decision:

- un Usuario con rol CLIENTE debe disponer de un perfil Cliente.
- el perfil no se elimina al retirar el rol; se marca inactivo.
- si el rol CLIENTE vuelve a asignarse, el perfil se reactiva.
- Cliente.idUsuario es unico cuando no es NULL.

Motivo:

Reserva requiere una identidad Cliente estable y trazable sin crear perfiles silenciosamente durante POST /api/reservas.

## 2026-10-16 - Secuencia global para codigo de reserva

Decision:

- SQL Server genera el consecutivo mediante dbo.ReservaCodigoSequence.
- Java construira el formato BEM-AAAA-NNNNNN.
- no se utilizara MAX(id) + 1 por riesgo de concurrencia.

## Decision FASE 9.0.8 - Refresh tokens y sesiones persistentes

Se adopta un modelo de sesion logica persistente basado en `SesionUsuario`, manteniendo Spring Security stateless respecto de HttpSession.

Decisiones:

- refresh token opaco y aleatorio;
- solamente SHA-256 queda persistido;
- rotacion en cada uso exitoso;
- expiracion absoluta de la sesion no se extiende durante refresh;
- revocacion actual mediante refresh token;
- revocacion masiva por usuario;
- listado de sesiones limitado al usuario autenticado;
- revocacion especifica combina idSesionUsuario + idUsuario;
- recursos de sesion inexistentes o ajenos responden 404;
- raw refresh token y refreshTokenHash no forman parte de DTOs de sesiones;
- no se agrega sessionId/jti al access JWT en 9.0.8;
- la revocacion de SesionUsuario no invalida inmediatamente access JWT ya emitidos;
- integracion server-side con ASP.NET Core MVC se difiere a FASE 9.0.9.

<!-- PHASE9_ADR_FINAL_START -->
## ADR - Cierre arquitectura de seguridad FASE 9

- Spring Boot conserva SessionCreationPolicy.STATELESS.
- Browser no recibe access JWT ni refresh token backend.
- ASP.NET MVC mantiene tokens backend server-side en Session.
- Refresh tokens son opacos, rotatorios y persistidos solo como hash.
- SesionUsuario controla revocacion persistente.
- Access JWT no se revoca inmediatamente al revocar refresh; permanece valido hasta expirar.
- No se agrega jti/session-id coupling en FASE 9.
- MFA TOTP usa AES-256-GCM y recovery codes SHA-256 one-time.
- TOTP y recovery comparten MFA_CHALLENGE.
- ENCARGADO_LOCAL usa resource hiding HTTP 404 fuera de su sede.
- Frontend nunca accede directamente a SQL Server.
<!-- PHASE9_ADR_FINAL_END -->
