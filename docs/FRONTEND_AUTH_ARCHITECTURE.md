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
