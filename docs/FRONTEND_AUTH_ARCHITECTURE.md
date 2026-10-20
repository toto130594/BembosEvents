# Bembos Events - Arquitectura de autenticacion del frontend

## Objetivo

El frontend ASP.NET Core MVC no reemplaza la seguridad del backend.

Spring Boot continua siendo la autoridad para:

- autenticacion de credenciales;
- validacion JWT;
- roles y authorities;
- autorizacion;
- scope por sede;
- respuestas 401 y 403.

## Flujo

```text
Browser
   |
   | Cookie HttpOnly de ASP.NET Core
   v
ASP.NET Core MVC
   |
   | JWT Bearer
   v
Spring Boot
   |
   v
Security / Service / Repository / SQL Server
```

## JWT

El navegador no guarda el JWT en:

- localStorage;
- sessionStorage;
- JavaScript;
- campos ocultos;
- query strings.

El JWT entregado por Spring Boot se conserva en estado de sesion
del servidor MVC.

El navegador recibe unicamente cookies HttpOnly administradas por
ASP.NET Core.

## Cookie MVC

La cookie MVC representa la sesion autenticada del portal.

Propiedades base:

- HttpOnly;
- SameSite=Lax;
- SecurePolicy=SameAsRequest para permitir desarrollo HTTP local;
- expiracion alineada con el JWT;
- sin sliding expiration por defecto.

En produccion HTTPS, Secure debe endurecerse a Always.

## Roles

El JWT actual no contiene claims de rol.

Por lo tanto, el frontend MVC no debe deducir ni inventar:

- ADMINISTRADOR;
- ENCARGADO_LOCAL;
- CLIENTE.

Spring Boot reconstruye las authorities a partir del subject autenticado.

Los menus o vistas especificos por rol solo se habilitaran cuando exista
una fuente backend confiable para consultar la identidad y roles actuales.

Ocultar elementos de interfaz nunca sustituye la autorizacion del backend.

## Backend Bearer Handler

Las llamadas del HttpClient tipado hacia Spring Boot pueden adjuntar el
JWT server-side mediante un DelegatingHandler.

Las URLs del backend continuan viniendo de configuracion.

No se hardcodea localhost o backend dentro del cliente HTTP.

## Data Protection

Las claves de ASP.NET Core Data Protection deben sobrevivir a la
recreacion del contenedor frontend.

En Docker se almacenan en un volumen nombrado y se montan en:

```text
/var/bembos-events/dpkeys
```

La aplicacion usa un ApplicationName estable:

```text
BembosEvents.Web
```

La persistencia evita invalidar cookies unicamente por recrear el
contenedor.

El cifrado de las claves en reposo mediante certificado o un gestor
externo de secretos corresponde al endurecimiento de produccion.

## Estado de sesion

En esta etapa la sesion server-side utiliza DistributedMemoryCache.

Esto es valido para el despliegue local de un solo contenedor frontend.

Una futura ejecucion multi-instancia debera sustituirlo por un almacen
distribuido compartido, por ejemplo Redis.

## Seguridad

El frontend nunca contiene:

- JWT secret de Spring Boot;
- password hash;
- credenciales de SQL Server;
- acceso directo a SQL Server.

El flujo obligatorio sigue siendo:

```text
ASP.NET Core MVC
        |
        v
Spring Boot REST API
        |
        v
Service
        |
        v
Repository / JPA
        |
        v
SQL Server
```

## Estado final FASE 9.0.9 - 2026-10-20

La integracion MVC de autenticacion queda completada.

Flujos disponibles:

- login normal;
- login con challenge MFA;
- TOTP;
- MFA recovery code;
- recuperacion y reset de password;
- reenvio de verificacion de correo;
- verificacion de correo;
- refresh automatico server-side;
- logout actual;
- listado de sesiones;
- revocacion individual;
- logout-all.

Estado sensible almacenado en ASP.NET Session:

- BackendJwt;
- BackendJwtExpiresAtUtc;
- BackendRefreshToken;
- BackendRefreshTokenExpiresAtUtc;
- PendingMfaChallengeToken y metadata temporal MFA cuando aplica.

El navegador recibe solamente cookies ASP.NET y los tokens de accion
que necesariamente pertenecen al usuario, como password reset o
email verification.

No existe replay automatico de requests despues de 401.

Hardening pendiente para 9.0.10:

- politica definitiva de Session IdleTimeout;
- lifecycle concurrency-safe de RefreshLocks.

## Hardening 9.0.10A - Session lifetime y refresh concurrency

Actualizado: 2026-10-20.

Politica de autenticacion MVC:

- ExpireTimeSpan de cookie: 1 hora;
- SlidingExpiration: false;
- Session IdleTimeout: 1 hora.

La duracion de la autenticacion del navegador esta desacoplada
del TTL absoluto del refresh token backend.

Refresh concurrency:

- un solo refresh backend por Session ID a la vez;
- requests concurrentes comparten una entrada de lock;
- la entrada mantiene reference counting;
- la entrada se elimina solamente cuando no quedan owners ni waiters
  registrados;
- no se dispone el SemaphoreSlim durante la eliminacion para evitar
  carreras con referencias rezagadas.

Este mecanismo protege la instancia MVC actual. Multi-instancia
queda fuera del alcance de FASE 9.
