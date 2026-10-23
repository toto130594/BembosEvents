# Bembos Events

Sistema para la gestion de eventos, reservas, pagos, sedes, salones y usuarios.

## Estado actual

**Fase actual:** FASE 7.6 - Auditoria definitiva de API REST.

FASE 9.0.7D - MFA durante login: COMPLETADA.

**Siguiente subfase:** FASE 7.6D.2 - Crear reserva real del cliente.

Actualmente el backend dispone de:

- Java 21.
- Spring Boot 3.4.x.
- Microsoft SQL Server 2022.
- Flyway.
- JPA / Hibernate.
- JWT.
- BCrypt.
- Spring Security.
- Roles CLIENTE, ENCARGADO_LOCAL y ADMINISTRADOR.
- UsuarioSede para limitar al encargado por sede.
- CRUD administrativo de usuarios.
- Perfil Cliente persistente para usuarios con rol CLIENTE.
- Secuencia SQL Server para generar codigos publicos de reserva.
- BackOffice con acceso global para ADMINISTRADOR.
- BackOffice restringido por sede para ENCARGADO_LOCAL.
- Reglas iniciales de reservas y pagos.
- Docker.
- JUnit 5 y Mockito.

## Arquitectura

```text
Browser
   |
   v
ASP.NET Core MVC
   | HTTP / JSON
   v
Spring Boot REST API
   |
   v
Service
   |
   v
Repository
   |
   v
JPA / Hibernate
   |
   v
SQL Server
```

El frontend ASP.NET Core MVC se implementara posteriormente.

## Roles

### CLIENTE

- Registro publico.
- Portal del cliente.
- Sin acceso administrativo al BackOffice.

### ENCARGADO_LOCAL

- Acceso al BackOffice.
- Debe tener una sede asignada.
- Solo puede acceder a informacion de su sede.

### ADMINISTRADOR

- Acceso administrativo global.
- Puede administrar usuarios.
- Puede consultar todas las sedes.

## Reservas

El backend es la fuente de verdad para:

- capacidad.
- disponibilidad.
- cruces de horario.
- monto minimo.
- precios.
- deposito.
- transiciones de estado.

Estados:

- PENDIENTE.
- CONFIRMADA.
- EN_PROCESO.
- COMPLETADA.
- CANCELADA.
- EXPIRADA.

## Pagos

Estados:

- PENDIENTE.
- APROBADO.
- RECHAZADO.
- CANCELADO.
- REEMBOLSADO.

El endpoint /api/pagos/validar-transicion es actualmente tecnico.

En FASE 7.6 se integrara esa regla dentro del endpoint real de cambio de estado.

## Seguridad futura

Planeado:

- recuperacion de password: IMPLEMENTADA en 9.0.5.
- verificacion de correo: IMPLEMENTADA en 9.0.5.
- MFA mediante TOTP: EN PROGRESO; enrollment implementado, login MFA pendiente.
- codigos de recuperacion.
- refresh tokens.
- gestion de sesiones.
- rate limiting: IMPLEMENTADO en 9.0.6.
- bloqueo temporal por intentos.
- auditoria administrativa.

## Chat e IA

Arquitectura objetivo:

```text
CLIENTE
   |
   v
IA
   |
   +---- resuelve consulta
   |
   +---- escala
           |
           v
     ENCARGADO_LOCAL
```

La IA nunca accedera directamente a SQL Server.

Toda operacion critica pasara por Services y reglas del backend.

## Levantar backend

```powershell
docker compose up -d --build backend
```

Ver estado:

```powershell
docker compose ps
```

Ver logs:

```powershell
docker compose logs --tail=100 backend
```

No ejecutar docker compose down -v salvo que se quiera destruir intencionalmente la base local.

## Validacion

```powershell
.\scripts\validate-backend.ps1
```

## Documentacion

- docs/ROADMAP.md
- docs/ARCHITECTURE.md
- docs/API.md
- docs/REST_CONTRACT.md
- docs/SECURITY.md
- docs/BUSINESS_RULES.md
- docs/PROJECT_RULES.md
- docs/DECISIONS.md
- docs/AI_CHAT.md
- docs/CHANGELOG.md
- docs/DATABASE_DESIGN.md

## Definition of Done

Una funcionalidad no esta terminada solamente porque compile.

Debe comprobarse:

1. Codigo.
2. Tests.
3. Build.
4. Migraciones si aplica.
5. Seguridad.
6. API.
7. Postman si aplica.
8. README.
9. Documentacion tecnica.
10. Git.

**Codigo actualizado con documentacion desactualizada = tarea incompleta.**

## Estado actual de seguridad avanzada

Implementado y validado:

- persistencia de seguridad mediante Flyway V8;
- bloqueo por intentos fallidos;
- auditoria de eventos de seguridad;
- recuperacion de password;
- verificacion de correo;
- rate limiting;
- primitivas TOTP;
- cifrado AES-256-GCM del secreto TOTP;
- setup y activacion MFA;
- generacion y almacenamiento seguro de recovery codes.

En progreso:

- MFA durante login;
- challenge temporal;
- consumo one-time de recovery codes durante login.

Pendiente posteriormente:

- refresh tokens;
- flujo completo de sesiones persistentes;
- integracion frontend de MFA.

## Estado MFA - FASE 9.0.7D completada

El backend ya soporta MFA TOTP durante login.

Cuando MFA esta habilitado:

- el password correcto no entrega JWT;
- se entrega un challenge temporal opaco;
- el usuario completa TOTP o utiliza un recovery code;
- solamente entonces se emite el JWT.

Controles implementados:

- challenge one-time;
- recovery one-time;
- locks pesimistas;
- auditoria MFA;
- failure audit REQUIRES_NEW;
- rate limit especifico MFA_CHALLENGE.

El frontend MFA todavia no forma parte de esta subfase.

## Estado actual

**Subfase actual:** FASE 9.0.10 - Smoke y hardening. 9.0.9 Integracion MVC completada el 2026-10-20.

<!-- PHASE10_README_FINAL_START -->
## FASE 10 - Chat e IA

Estado: COMPLETADA.

Implementado:

- conversaciones y mensajes persistidos;
- chat REST Cliente y BackOffice;
- WebSocket/STOMP backend;
- IA basica mediante provider abstraction;
- proveedor OpenAI-compatible opcional;
- escalamiento a ENCARGADO_LOCAL por sede;
- historial;
- notificaciones realtime BackOffice;
- MVC Cliente y BackOffice;
- hardening de ownership, sede y STOMP.

El browser no recibe JWT ni refresh token backend y no conecta directamente al WebSocket de Spring Boot.

Siguiente fase planificada: FASE 11 - Mercado Pago.
<!-- PHASE10_README_FINAL_END -->

<!-- PHASE12_ADVANCED_AI_START -->
## FASE 12 - IA avanzada

Estado: **COMPLETADA**.

Incluye:

- typed AI tools;
- disponibilidad conversacional;
- recomendaciones deterministicas;
- borrador de reserva server-side;
- confirmacion explicita con revalidacion;
- RAG publico con `kb://` attribution;
- analitica IA;
- dashboard BackOffice;
- prompt-injection hardening;
- rate/cost controls;
- MVC conversational UX;
- E2E + failure modes.

Documento canonico: `docs/AI_ADVANCED.md`.
<!-- PHASE12_ADVANCED_AI_END -->
