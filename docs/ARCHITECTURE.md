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

## Arquitectura futura de MFA

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
