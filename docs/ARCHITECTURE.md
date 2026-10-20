# Arquitectura - Bembos Events

## Objetivo

Bembos Events utiliza una arquitectura separada entre frontend, backend y base de datos.

El backend Spring Boot es la autoridad de negocio.

## Arquitectura general

```text
Browser
   |
   v
ASP.NET Core MVC
   |
   | HTTP / JSON
   v
Spring Boot REST API
   |
   v
Controller
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
Microsoft SQL Server
```

## Frontend

Tecnologia planificada:

- ASP.NET Core MVC.

El frontend tendra dos areas principales:

### Portal publico / cliente

- registro.
- login.
- consulta de sedes.
- consulta de salones.
- consulta de paquetes.
- reserva.
- pagos.
- historial.
- chat.

### BackOffice

Compartido por:

- ADMINISTRADOR.
- ENCARGADO_LOCAL.

El contenido visible dependera del rol y del alcance por sede.

## Backend

Tecnologia:

- Java 21.
- Spring Boot 3.4.x.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- Bean Validation.
- Flyway.

Flujo preferido:

```text
Controller
   |
   v
Service interface
   |
   v
ServiceImpl
   |
   v
Repository
```

## Base de datos

Motor:

- Microsoft SQL Server 2022.

Flyway es la fuente de verdad para la evolucion del esquema.

## Autenticacion

Flujo actual:

```text
Correo + password
      |
      v
POST /api/auth/login
      |
      v
Spring Security
      |
      v
JWT
```

Los passwords se almacenan mediante BCrypt.

## Autorizacion

Roles:

- CLIENTE.
- ENCARGADO_LOCAL.
- ADMINISTRADOR.

## Alcance por sede

El rol determina que puede hacer el usuario.

UsuarioSede determina donde puede hacerlo.

```text
Usuario
   |
   v
UsuarioRol
   |
   v
ENCARGADO_LOCAL
   |
   v
UsuarioSede
   |
   v
Sede autorizada
```

ADMINISTRADOR tiene alcance global.

ENCARGADO_LOCAL tiene alcance limitado a una sede.

CLIENTE no utiliza UsuarioSede para funciones administrativas.

## Reservas

Las reglas de reservas pertenecen al backend.

El backend valida:

- capacidad.
- disponibilidad.
- superposicion.
- monto minimo.
- deposito.
- precios.
- estados.

## Pagos

Las transiciones de estado deben validarse dentro de PagoService.

Los endpoints tecnicos actuales de validacion no representan necesariamente la API final.

FASE 7.6 definira el flujo REST definitivo.

## Arquitectura MFA por etapas

```text
Correo + password
      |
      v
Credenciales correctas
      |
      v
MFA requerido?
   |       |
   NO      SI
   |       |
   v       v
  JWT   codigo TOTP
            |
            v
           JWT
```

Mecanismo previsto:

- TOTP.
- codigos de recuperacion.

## Arquitectura futura del chat

```text
CLIENTE
   |
   v
CHAT
   |
   v
IA
   |
   +---- Resuelve consulta
   |
   +---- Escala a humano
                |
                v
         ENCARGADO_LOCAL
                |
                v
          misma conversacion
```

## IA y backend

La IA nunca debe acceder directamente a SQL Server.

Flujo permitido:

```text
IA
 |
 v
API / Services
 |
 v
Reglas de negocio
 |
 v
Repositories
 |
 v
SQL Server
```

## Eventos internos futuros

Se evaluara utilizar eventos internos para desacoplar funcionalidades.

Ejemplos:

- ReservaCreadaEvent.
- ReservaCanceladaEvent.
- PagoAprobadoEvent.
- ChatEscaladoEvent.

Estos eventos podran activar:

- historial.
- notificaciones.
- correo.
- WhatsApp.
- dashboard.

## Principio arquitectonico

Ni el frontend ni la IA pueden saltarse las reglas del backend.

Toda accion critica debe pasar por Services.

### Estado de implementacion MFA

Implementado hasta 9.0.7C:

- primitivas TOTP;
- proteccion AES-256-GCM;
- setup autenticado;
- activacion autenticada;
- recovery codes.

Pendiente en 9.0.7D:

- bifurcacion del login;
- challenge MFA temporal;
- TOTP/recovery como segundo factor;
- emision de JWT solamente despues de completar MFA.

El frontend nunca debe tratar un challenge MFA como un access token.

## Estado implementado MFA login 9.0.7D

Arquitectura efectiva:

Cliente
  |
  v
POST /api/auth/login
  |
  +-- MFA OFF --> JwtService --> access JWT
  |
  +-- MFA ON --> MFA_LOGIN_CHALLENGE
                    |
                    +-- POST /api/auth/mfa/verificar
                    |       |
                    |       +-- TOTP correcto
                    |
                    +-- POST /api/auth/mfa/recuperacion
                            |
                            +-- recovery code correcto
                                    |
                                    v
                              consume challenge
                                    |
                                    v
                                JwtService
                                    |
                                    v
                                access JWT

Persistencia:

- UsuarioSeguridad conserva estado MFA y secreto TOTP protegido;
- TokenSeguridadUsuario conserva hash SHA-256 del challenge;
- MfaRecoveryCode conserva solamente hash SHA-256 y fechaUso.

El frontend no fue modificado en 9.0.7D.

## Arquitectura de sesiones persistentes - FASE 9.0.8

El backend continua utilizando Spring Security con `SessionCreationPolicy.STATELESS`.

La sesion de seguridad se modela mediante `SesionUsuario`:

```text
Access JWT
    |
    | autenticacion stateless
    v
Spring Security

Refresh token
    |
    | SHA-256
    v
SesionUsuario
    |
    +-- fechaCreacion
    +-- fechaExpiracion
    +-- fechaUltimoUso
    +-- fechaRevocacion
    +-- userAgent
    +-- ipOrigen
```

`SesionUsuario` no es una HttpSession.

El refresh token raw:

- se entrega una sola vez en cada emision/rotacion;
- nunca se persiste;
- se valida contra su hash;
- se rota en cada refresh exitoso.

Gestion de sesiones:

```text
GET /api/auth/sessions
    -> usuario autenticado
    -> sesiones activas propias

DELETE /api/auth/sessions/{id}
    -> usuario autenticado
    -> idSesion + idUsuario
    -> PESSIMISTIC_WRITE
    -> revocacion

POST /api/auth/logout-all
    -> usuario autenticado
    -> revocacion masiva
```

No existe acoplamiento entre access JWT y `SesionUsuario` mediante sessionId/jti.

Por esa razon:

```text
revocar refresh/session
        !=
revocar inmediatamente access JWT
```

El access JWT ya emitido conserva validez hasta su expiracion.

La integracion de estos mecanismos con ASP.NET Core MVC corresponde a FASE 9.0.9.

## Integracion MVC de autenticacion - FASE 9.0.9

Actualizado: 2026-10-20.

El frontend MVC funciona como frontera server-side para la
autenticacion backend.

Browser
  |
  | Cookie MVC + cookie ASP.NET Session
  v
ASP.NET Core MVC
  |
  | Authorization: Bearer <access JWT>
  | refresh token solo en Session server-side
  v
Spring Boot

El navegador no persiste access JWT ni refresh token en:

- localStorage;
- sessionStorage;
- JavaScript;
- Razor hidden fields;
- query strings.

Excepciones deliberadas:

- password-reset token puede ser recibido por el usuario;
- email-verification token puede ser recibido por el usuario.

Estos tokens de accion no equivalen a JWT/refresh y poseen
lifecycle temporal controlado por Spring Boot.

El refresh del access token es preemptivo. El DelegatingHandler
no reenvia automaticamente un request despues de recibir 401.

<!-- PHASE9_ARCH_FINAL_START -->
## Arquitectura de autenticacion final - FASE 9

Browser -> ASP.NET Core MVC -> Spring Boot REST API -> Service -> Repository -> SQL Server.

- Browser usa cookie ASP.NET de autenticacion y cookie de Session.
- JWT backend y refresh permanecen server-side en ASP.NET Session.
- Frontend nunca accede directamente a SQL Server.
- Spring Boot es autoridad final para autenticacion, autorizacion y reglas de negocio.
- Refresh automatico es proactivo y controlado; no existe replay reactivo automatico tras 401.
- MFA se completa antes de crear la sesion autenticada MVC.
- SesionUsuario representa la sesion persistente logica backend.
- Logout revoca refresh backend y limpia estado local MVC.
- Logout-all revoca sesiones persistentes y limpia la sesion MVC actual.
- Multi-instancia ASP.NET requerira almacenamiento distribuido de Session; queda fuera del alcance de FASE 9.
<!-- PHASE9_ARCH_FINAL_END -->
