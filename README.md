# Bembos Events

Sistema para la gestion de eventos, reservas, pagos, sedes, salones y usuarios.

## Estado actual

**Fase actual:** FASE 7.6 - Auditoria definitiva de API REST.

**Subfase actual:** FASE 7.6D - Reservas del cliente. 7.6D.1 completada; 7.6D.2 es la siguiente implementacion.

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

- recuperacion de password.
- verificacion de correo.
- MFA mediante TOTP.
- codigos de recuperacion.
- refresh tokens.
- gestion de sesiones.
- rate limiting.
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
