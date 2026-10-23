# Roadmap - Bembos Events

## Objetivo

Construir Bembos Events de manera incremental, manteniendo backend, base de datos, seguridad, frontend y documentacion sincronizados.

# FASE 1 - Modelo de datos

Estado: COMPLETADA.

- entidades.
- relaciones.
- reglas iniciales.

# FASE 2 - SQL Server

Estado: COMPLETADA.

- tablas.
- claves.
- restricciones.
- indices.

# FASE 3 - Spring Boot

Estado: COMPLETADA.

- Java 21.
- Spring Boot.
- Maven.
- Docker.

# FASE 4 - Persistencia

Estado: COMPLETADA.

- JPA.
- entidades.
- repositories.
- Flyway.

# FASE 5 - Reglas de negocio

Estado: COMPLETADA.

- estados de reserva.
- estados de pago.
- capacidad.
- disponibilidad.
- monto minimo.
- deposito.
- transiciones.

# FASE 6 - API tecnica inicial

Estado: COMPLETADA.

- DTO.
- Controllers.
- excepciones.
- endpoints tecnicos de validacion.
- tests.

# FASE 7 - Seguridad

Estado: COMPLETADA.

- BCrypt.
- JWT.
- Spring Security.
- registro.
- login.
- roles.

# FASE 7.5 - Administracion y alcance por sede

Estado: COMPLETADA.

- UsuarioSede.
- CRUD administrativo de usuarios.
- ADMINISTRADOR global.
- ENCARGADO_LOCAL por sede.
- BackOffice de sedes.
- pruebas de autorizacion.
- Postman por roles.

# FASE 7.5F - Sincronizacion documental

Estado: COMPLETADA.

- README.
- reglas para agentes.
- reglas de proyecto.
- arquitectura.
- decisiones.
- negocio.
- seguridad.
- API.
- IA y chat.
- changelog.

# FASE 7.6 - Auditoria REST

Estado: COMPLETADA.

Subfase actual: CERRADA.

Objetivo:

Definir la API REST definitiva antes del frontend.

Avance:

- 7.6B - Contrato REST definitivo: COMPLETADA.
- 7.6C.1 - Catalogos publicos: COMPLETADA.
- 7.6C.2 - Disponibilidad publica: COMPLETADA.
- 7.6D.1 - Consistencia Usuario/Cliente: COMPLETADA.
- 7.6D.2 - Crear reserva real del cliente: COMPLETADA.
- 7.6D.3 - Mis reservas del cliente: COMPLETADA.
- 7.6D.4 - Consultar una reserva propia: COMPLETADA.
- 7.6D.5 - Cancelacion de una reserva propia: COMPLETADA.
- 7.6E - Reservas BackOffice: COMPLETADA.
  - 7.6E.1 - Listar reservas BackOffice: COMPLETADA.
  - 7.6E.2 - Consultar reserva BackOffice: COMPLETADA.
  - 7.6E.3 - Cambiar estado de reserva BackOffice: COMPLETADA.
  - 7.6E.4 - Documentacion, Postman y Git: COMPLETADA.
- 7.6F - Pagos base: COMPLETADA.
  - 7.6F.1 - Pagos Cliente: COMPLETADA.
    - iniciar Pago de una Reserva propia: COMPLETADO.
    - listar Pagos de una Reserva propia: COMPLETADO.
    - consultar Pago propio por id: COMPLETADO.
  - 7.6F.2 - Pagos BackOffice: COMPLETADA.
    - listar Pagos BackOffice: COMPLETADO.
    - consultar Pago BackOffice por id: COMPLETADO.
  - 7.6F.3 - Seguridad y regresion integral: COMPLETADA.
  - 7.6F.4 - Documentacion, Postman y Git: COMPLETADA.

- 7.6G - Retirar endpoints tecnicos: COMPLETADA.
- 7.6H - Documentacion, Postman y tests: COMPLETADA.
- 7.6I - Validacion final y runtime smoke test: COMPLETADA.
Revisar:

- Auth.
- Usuarios.
- Sedes.
- Salones.
- Horarios.
- Paquetes.
- Tematicas.
- Servicios adicionales.
- Reservas.
- Pagos.

Cada endpoint se clasificara como:

- DEFINITIVO.
- TECNICO.
- REEMPLAZAR.
- ELIMINAR.
- PENDIENTE.

Siguiente fase: 7.7 - Seguridad completa por sede.
# FASE 7.7 - Seguridad por sede completa

Cierre: 7.7.6 - Runtime smoke y cierre: COMPLETADA.

Estado: COMPLETADA.

Aplicar alcance real de ENCARGADO_LOCAL a:

- reservas.
- pagos.
- operaciones administrativas relacionadas.
## Progreso 7.7

- 7.7.1 - Auditoria de seguridad por sede: COMPLETADA.
- 7.7.2 - Auditoria focalizada de hardening: COMPLETADA.
- 7.7.3 - Hardening de Sedes BackOffice: COMPLETADA.
- 7.7.4 - Regresion completa: COMPLETADA.
- 7.7.5 - Documentacion y Postman: COMPLETADA.
- 7.7.6 - Runtime smoke y cierre: COMPLETADA.

Siguiente fase: FASE 8.
# FASE 8 - Frontend ASP.NET Core MVC

Estado: COMPLETADA.

## Portal publico y cliente

- home.
- registro.
- login.
- sedes.
- salones.
- paquetes.
- disponibilidad.
- reservas.
- pagos.

## BackOffice

- dashboard.
- usuarios.
- reservas.
- pagos.
- gestion por sede.

## Cierre FASE 8 - 8.0.12

FASE 8 completada y validada.

Componentes cerrados:

- base ASP.NET Core MVC.
- integracion HttpClient con Spring Boot.
- layout publico.
- home.
- registro y login.
- catalogos publicos.
- flujo completo de reservas.
- flujo de pagos del cliente.
- BackOffice MVC.
- seguridad por roles y alcance por sede.
- anti-enumeration.
- JWT exclusivamente server-side.

Checkpoints principales:

- 743fc1a - catalogos publicos 8.0.8.
- 3d01df5 - reservas frontend 8.0.9.
- d3f1837 - pagos frontend 8.0.10.
- f19e54b - BackOffice MVC 8.0.11.

Validacion final:

- backend: 165 tests OK.
- frontend Release: 0 warnings.
- Docker frontend: OK.
- ADMINISTRADOR: alcance global validado.
- ENCARGADO_LOCAL: scope por sede validado.
- CLIENTE: BackOffice denegado.
- SQL directo desde frontend: NO.
- JWT en navegador: NO.

Siguiente fase:

- FASE 9 - Seguridad avanzada.

# FASE 9 - Seguridad avanzada

Estado: COMPLETADA.

- recuperacion de password.
- verificacion de correo.
- MFA TOTP.
- recovery codes.
- refresh tokens.
- sesiones.
- rate limiting.
- intentos fallidos.
- auditoria.

# FASE 10 - Chat e IA

Estado: COMPLETADA.

Subfases:

- 10.0.1 - Auditoria + diseno exacto: COMPLETADA.
- 10.0.2 - Persistencia Conversacion/Mensaje + Flyway: COMPLETADA.
- 10.0.3 - REST Chat base + ownership/scope: COMPLETADA.
- 10.0.4 - WebSocket realtime: COMPLETADA.
- 10.0.5 - IA provider abstraction + respuesta basica: COMPLETADA.
- 10.0.6 - Escalamiento IA -> ENCARGADO_LOCAL por sede: COMPLETADA.
- 10.0.7 - Historial + notificaciones: COMPLETADA.
- 10.0.8 - MVC Cliente + BackOffice chat: COMPLETADA.
- 10.0.9 - Smoke + security hardening: COMPLETADA.
- 10.0.10 - Documentacion + cierre FASE 10: COMPLETADA tecnicamente y documentalmente; checkpoint commit pendiente de autorizacion explicita.

Incluye:

- conversaciones y mensajes persistidos;
- ownership de CLIENTE;
- scope de ENCARGADO_LOCAL por sede;
- ADMINISTRADOR con alcance global de lectura BackOffice;
- WebSocket/STOMP backend con topics por conversacion;
- proveedor IA abstracto con proveedor basico y proveedor OpenAI-compatible opcional;
- respuesta IA persistida como MensajeChat de tipo IA;
- IA activa solamente mientras la conversacion permanece en BOT;
- escalamiento explicito BOT -> ESPERANDO_ENCARGADO -> EN_ATENCION;
- mensajes humanos del ENCARGADO_LOCAL;
- historial basado en MensajeChat;
- notificaciones realtime BackOffice globales y por sede;
- MVC Cliente y BackOffice;
- JWT y refresh token backend conservados server-side;
- browser sin conexion directa al WebSocket de Spring Boot;
- hardening STOMP CONNECT, SUBSCRIBE y SEND;
- bloqueo de SEND directo a /topic/** y topics de notificaciones;
- limite de mensaje de 4000 caracteres;
- validacion final de backend, frontend y runtime.

Limites deliberados de FASE 10:

- no se implementan heuristicas de intent detection para escalamiento;
- no se implementa bandeja persistente read/unread de notificaciones;
- no se implementan RAG, recomendaciones inteligentes, borradores de reserva, disponibilidad conversacional, analitica ni dashboard inteligente;
- esas capacidades permanecen en FASE 12.

Commit y push no se ejecutan automaticamente.
# FASE 11 - Mercado Pago

Estado: COMPLETADA.

Avance:

- 11.0.1 - Auditoria y diseno exacto: COMPLETADA.
- 11.0.2 - Configuracion TEST + provider Orders API: COMPLETADA.
- 11.0.3 - CLIENTE iniciar pago + Checkout Pro Orders API: COMPLETADA.
- 11.0.4 - Webhook endpoint: COMPLETADA.
- 11.0.5 - Validacion HMAC-SHA256 de webhook: COMPLETADA.
- 11.0.6 - Idempotencia + eventos persistentes de proveedor: COMPLETADA.
- 11.0.7 - Sincronizacion de estado desde Order: COMPLETADA.
- 11.0.8 - Reembolso total: COMPLETADA.
- 11.0.9 - Reconciliacion manual individual y por lote: COMPLETADA.
- 11.0.10 - MVC Cliente + BackOffice: COMPLETADA.
- 11.0.11 - Smoke + security hardening: COMPLETADA.
- 11.0.12 - Documentacion + cierre formal: COMPLETADA.

Alcance deliberado:

- Checkout Pro + Orders API.
- credenciales TEST solamente.
- provider deshabilitado por defecto.
- sin cargos reales durante validacion.
- reembolso total solamente.
- sin produccion.
- sin reembolso parcial.
- sin reconciliacion programada.

Documento canonico:

`docs/MERCADO_PAGO.md`

# FASE 12 - IA avanzada

Estado: COMPLETADA.

Avance:

- 12.0.1 - Audit + freeze: COMPLETADA.
- 12.0.2 - Typed tools + availability: COMPLETADA.
- 12.0.3 - Recommendation engine: COMPLETADA.
- 12.0.4 - Server-side conversational drafts: COMPLETADA.
- 12.0.5 - RAG knowledge + attribution: COMPLETADA.
- 12.0.6 - Orchestration + safe tool execution: COMPLETADA.
- 12.0.7 - Analytics event: COMPLETADA.
- 12.0.8 - Intelligent BackOffice dashboard: COMPLETADA.
- 12.0.9 - Prompt-injection + rate/cost/security hardening: COMPLETADA.
- 12.0.10 - MVC conversational UX integration: COMPLETADA.
- 12.0.11 - E2E + failure modes: COMPLETADA.
- 12.0.12 - Documentation + formal close: COMPLETADA.

Capacidades cerradas:

- disponibilidad conversacional;
- recomendaciones deterministicas;
- borradores server-side;
- confirmacion explicita con revalidacion del dominio;
- RAG publico con source attribution;
- orquestacion con typed tool allowlist;
- analitica sin contenido sensible;
- dashboard BackOffice read-only;
- prompt-injection hardening;
- rate limit y provider budget;
- MVC conversacional seguro;
- E2E de failure modes.

Documento canonico:

`docs/AI_ADVANCED.md`

# Regla del roadmap

No se debe marcar una fase como COMPLETADA mientras su codigo y documentacion no esten sincronizados.

## Avance detallado FASE 9 - 2026-10-19

Estado general de FASE 9: COMPLETADA.

Subfases verificadas:

- 9.0.3 - Persistencia y primitivas de seguridad: COMPLETADA.
- 9.0.4 - Auditoria e intentos fallidos: COMPLETADA.
- 9.0.5 - Recuperacion de password y verificacion de correo: COMPLETADA.
- 9.0.6 - Rate limiting: COMPLETADA.
- 9.0.7A - Auditoria MFA TOTP + recovery codes: COMPLETADA.
- 9.0.7B - Crypto + primitivas TOTP: COMPLETADA.
- 9.0.7C - Enrollment MFA: COMPLETADA.
- 9.0.7D - MFA durante login: COMPLETADA.
- 9.0.8 - Refresh tokens y sesiones: COMPLETADA.

9.0.7C incluye:

- setup MFA;
- activacion MFA;
- AES-256-GCM para secreto TOTP;
- RFC 6238;
- recovery codes almacenados solamente mediante SHA-256;
- activacion atomica.

9.0.7D completada con:

- challenge MFA;
- bifurcacion del login segun `mfaHabilitado`;
- validacion TOTP durante login;
- consumo one-time de recovery code;
- rate limiting del segundo factor.

FASE 9 completa permanece abierta hasta finalizar las subfases restantes.

## Cierre 9.0.7D - 2026-10-19

FASE 9.0.7D - MFA durante login: COMPLETADA.

Incluye:

- challenge temporal MFA_LOGIN_CHALLENGE;
- Flyway V9;
- bloqueo de JWT antes del segundo factor;
- verificacion TOTP;
- recovery code de un solo uso;
- auditoria de exito y fallo;
- MFA_VERIFICACION_FALLIDA con REQUIRES_NEW;
- rate limit MFA_CHALLENGE;
- pruebas de replay y concurrencia;
- auditoria final 9.0.7D-8 aprobada.

FASE 9 permanece abierta hasta completar sus subfases restantes.

## Cierre 9.0.8 - 2026-10-19

FASE 9.0.8 - Refresh tokens + sesiones: COMPLETADA.

Incluye:

- refresh token opaco de 256 bits;
- solamente SHA-256 del refresh token queda persistido;
- rotacion one-time en cada refresh;
- reutilizacion de refresh token anterior rechazada;
- expiracion absoluta de la sesion preservada durante rotacion;
- `SesionUsuario` como sesion logica persistente, no HttpSession;
- `POST /api/auth/refresh`;
- `POST /api/auth/logout`;
- `POST /api/auth/logout-all`;
- `GET /api/auth/sessions`;
- `DELETE /api/auth/sessions/{idSesionUsuario}`;
- ownership por usuario para administracion de sesiones;
- sesion inexistente o ajena responde 404 para evitar enumeracion;
- PESSIMISTIC_WRITE en rotacion y revocacion sensible;
- auditoria SESION_CREADA;
- auditoria SESION_RENOVADA;
- auditoria SESION_REVOCADA;
- auditoria TODAS_SESIONES_REVOCADAS;
- refreshTokenHash nunca se expone mediante la API;
- access JWT no se revoca inmediatamente y permanece valido hasta su expiracion;
- no existe acoplamiento JWT sessionId/jti en esta subfase;
- auditoria final 9.0.8G e implementacion de gestion de sesiones 9.0.8I aprobadas;
- backend validator, tests y Docker build aprobados.

FASE 9 permanece EN PROGRESO.

Siguiente subfase:

- 9.0.9 - Integracion MVC.

## Cierre 9.0.9 - Integracion MVC - 2026-10-20

FASE 9.0.9 - Integracion MVC: COMPLETADA.

Objetivos completados:

- recuperacion de password integrada en MVC;
- verificacion de correo integrada en MVC;
- login MFA TOTP y recovery code integrado en MVC;
- JWT backend almacenado solamente server-side;
- refresh token backend almacenado solamente server-side;
- refresh automatico preemptivo y controlado;
- rotacion de refresh token actualizada server-side;
- logout MVC con revocacion backend;
- listado de sesiones propias;
- revocacion de una sesion propia;
- cierre de todas las sesiones;
- limpieza local de JWT, refresh, expiraciones y cookie MVC;
- proteccion CSRF en acciones MVC mutables;
- sin retry reactivo ante HTTP 401;
- sin replay automatico de requests;
- JWT, refresh token, MFA challenge y hashes de sesion no llegan al browser.

Validaciones finales:

- 9.0.9D MFA MVC: VALIDADA;
- 9.0.9E Password Recovery MVC: VALIDADA;
- 9.0.9F Email Verification MVC: VALIDADA;
- 9.0.9G Session Management UI: VALIDADA;
- 9.0.9H auditoria integral MVC: APROBADA;
- limpieza parcial de auth en PagoController: CORREGIDA;
- limpieza parcial de auth en ReservaController: CORREGIDA;
- frontend Release build: OK.

Hardening diferido a 9.0.10:

1. definir y documentar la politica definitiva de ASP.NET Session IdleTimeout;
2. resolver el lifecycle de RefreshLocks con una estrategia concurrency-safe.

No son bloqueantes para 9.0.9.

Estado general de FASE 9: EN PROGRESO.

Siguiente subfase:

- 9.0.10 - Smoke y hardening.

<!-- PHASE9_FINAL_CLOSE_START -->
## Cierre formal FASE 9 - 2026-10-20

Estado final: COMPLETADA.

Subfases finales verificadas:

- 9.0.7 - MFA TOTP + recovery codes: COMPLETADA.
- 9.0.8 - Refresh tokens + sesiones persistentes: COMPLETADA.
- 9.0.9 - Integracion MVC de seguridad: COMPLETADA.
- 9.0.10 - Smoke + hardening: COMPLETADA.
- 9.0.11 - Cierre formal: COMPLETADA tecnicamente y documentalmente; checkpoint commit pendiente de autorizacion explicita.

Validacion final:

- login normal y controles de bloqueo;
- password reset y verificacion de correo;
- MFA TOTP y recovery code one-time;
- refresh rotation y true reuse detection;
- logout, logout-all y sesiones persistentes;
- rate limiting LOGIN y MFA con HTTP 429 + Retry-After;
- auditoria RATE_LIMIT_EXCEDIDO verificada en SQL;
- CLIENTE, ENCARGADO_LOCAL y ADMINISTRADOR;
- resource hiding cross-sede mediante HTTP 404;
- backend validator, tests y Docker build OK;
- frontend Release build OK;
- backend runtime HTTP 200;
- frontend Docker runtime HTTP 200;
- SQL Server healthy;
- sin fallos transaccionales/Hikari en regresion final.

Commit y push no se ejecutan automaticamente.
<!-- PHASE9_FINAL_CLOSE_END -->
