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

La integracion con Mercado Pago soporta idempotencia persistente; un evento procesado no debe procesarse dos veces.

Un mismo evento del proveedor no debe procesarse dos veces.

## ADR-018 - Webhooks seguros

Los webhooks de Mercado Pago validan firma HMAC-SHA256 antes de persistir eventos o modificar Pagos.

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

<!-- PHASE10_DECISIONS_FINAL_START -->
## Decisiones cerradas de FASE 10 - Chat e IA

1. Conversacion y MensajeChat son la fuente persistente del historial de chat.
2. La IA se integra mediante ChatAiProvider; no se acopla el dominio a un proveedor concreto.
3. El proveedor basico es el default para que el sistema funcione sin credenciales externas.
4. Un proveedor OpenAI-compatible puede habilitarse por configuracion.
5. La IA responde solamente en estado BOT.
6. El escalamiento de FASE 10 es explicito; no se inventan heuristicas de intent detection.
7. Una conversacion escalada requiere sede.
8. ENCARGADO_LOCAL solamente atiende conversaciones dentro de su UsuarioSede.
9. ADMINISTRADOR conserva lectura global pero no toma conversaciones como ENCARGADO_LOCAL.
10. Las notificaciones de FASE 10 son realtime y no una bandeja persistente.
11. Browser no recibe JWT ni refresh token backend.
12. Browser no conecta directamente al WebSocket Spring Boot; MVC usa polling same-origin.
13. STOMP SEND directo al broker se bloquea; el envio de cliente pasa por /app y ownership.
14. Funciones IA avanzadas permanecen en FASE 12.
<!-- PHASE10_DECISIONS_FINAL_END -->

<!-- PHASE11_MERCADOPAGO_DECISIONS_START -->
## ADR-020 - Mercado Pago mediante Checkout Pro Orders API

Decision:

- usar Checkout Pro sobre Orders API;
- crear Orders desde Spring Boot;
- mantener Access Token solo server-side;
- guardar Order id en `Pago.referenciaExterna`;
- utilizar `checkout_url` solo como redireccion del comprador;
- nunca aprobar un Pago a partir de datos del navegador.

La configuracion de Fase 11 es TEST y el provider permanece deshabilitado por defecto.

## ADR-021 - Eventos de proveedor persistentes

Decision:

- persistir eventos verificados en `EventoPagoProveedor`;
- unicidad por `proveedor + idEventoExterno`;
- preferir notification id;
- utilizar fingerprint SHA-256 como fallback;
- permitir retry si el evento existe pero no esta procesado;
- ignorar duplicados ya procesados.

## ADR-022 - Reembolsos Fase 11

Decision:

- implementar solamente reembolso total;
- requerir ADMINISTRADOR;
- consultar Order antes del reembolso;
- usar idempotency key deterministico;
- actualizar localmente a REEMBOLSADO solamente luego de confirmacion externa;
- postergar reembolsos parciales hasta que el dominio modele importes parciales.
<!-- PHASE11_MERCADOPAGO_DECISIONS_END -->

<!-- PHASE12_ADVANCED_AI_DECISIONS_START -->
## ADR-023 - Orquestacion deterministica con typed tools

Decision:

- usar `AiIntentInterpreter` deterministico;
- ejecutar solo tools registrados en `AiToolRegistry`;
- validar input/output types en runtime;
- no requerir native function calling;
- no aceptar nombres de tools provenientes del modelo.

## ADR-024 - Borrador conversacional server-side

Decision:

- persistir un borrador por Conversacion;
- no crear Reserva durante captura incremental;
- confirmar solamente con frase explicita;
- delegar creacion final a `ReservaClienteService`.

## ADR-025 - RAG provider-agnostic

Decision:

- usar `KnowledgeRetriever`;
- corpus publico allowlisted;
- recuperacion lexica deterministica inicial;
- source URI `kb://`;
- no vector DB obligatorio en FASE 12.

## ADR-026 - Analitica sin contenido sensible

Decision:

- persistir metadata operacional;
- no persistir prompts/respuestas/email/payloads RAG;
- usar transaccion independiente;
- no romper chat si analytics falla.

## ADR-027 - Seguridad y control de costo IA

Decision:

- security guard antes del intent interpreter;
- rate limit por sujeto hasheado;
- provider budget separado;
- costo controlado por cantidad de invocaciones;
- no afirmar token usage cuando provider no lo reporta.

## ADR-028 - MVC como frontera del browser

Decision:

- reutilizar MVC existente;
- mantener JWT/refresh token server-side;
- no conectar browser directo al backend WebSocket;
- sugerencias de UX no ejecutan acciones automaticamente;
- Spring Boot conserva autoridad final.
<!-- PHASE12_ADVANCED_AI_DECISIONS_END -->
