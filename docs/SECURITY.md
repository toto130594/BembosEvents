# Seguridad - Bembos Events

Este documento diferencia medidas IMPLEMENTADAS de mejoras PLANEADAS.

# 1. Seguridad implementada

## Passwords

Los passwords se almacenan mediante BCrypt.

Nunca se deben almacenar passwords en texto plano.

Los passwords originales no pueden recuperarse desde la base de datos.

## JWT

El backend utiliza autenticacion mediante Bearer JWT.

Flujo actual:

```text
Correo + password
      |
      v
POST /api/auth/login
      |
      v
validacion de credenciales
      |
      v
JWT
```

## Spring Security

Spring Security protege los recursos REST.

El backend valida roles independientemente del frontend.

## Registro publico

El registro publico siempre crea CLIENTE.

El registro publico crea tambien un perfil Cliente activo asociado al Usuario.

Un usuario no puede utilizar el registro publico para convertirse en ADMINISTRADOR o ENCARGADO_LOCAL.

## Acceso por rol

CLIENTE:

- sin acceso BackOffice.

ENCARGADO_LOCAL:

- acceso BackOffice limitado por sede.

ADMINISTRADOR:

- acceso administrativo global.

## UsuarioSede

UsuarioSede se utiliza para comprobar la sede autorizada de ENCARGADO_LOCAL.

La comprobacion de sede debe realizarse en backend.

## CSRF

La API REST actual utiliza autenticacion Bearer stateless.

CSRF se encuentra deshabilitado para este modelo de API.

La decision debera revisarse si posteriormente cambia el mecanismo de autenticacion o se utilizan cookies de sesion.

## Secretos

Nunca deben versionarse:

- .env.
- passwords.
- JWT.
- JWT_SECRET.
- claves de proveedores.
- credenciales de base de datos.
- secretos TOTP.
- recovery codes sin proteccion.

# 2. MFA - planeado

Se utilizara TOTP.

Compatible con aplicaciones como:

- Microsoft Authenticator.
- Google Authenticator.
- Authy.
- aplicaciones TOTP compatibles.

Flujo previsto:

```text
correo + password
       |
       v
credenciales validas
       |
       v
MFA activo?
   |         |
   NO        SI
   |         |
   v         v
  JWT    codigo TOTP
                |
                v
               JWT
```

Politica objetivo:

- CLIENTE: MFA opcional.
- ENCARGADO_LOCAL: MFA obligatorio.
- ADMINISTRADOR: MFA obligatorio.

El secreto TOTP nunca debe almacenarse sin proteccion.

# 3. Codigos de recuperacion - planeado

Al activar MFA se generaran codigos de recuperacion.

Reglas:

- uso unico.
- almacenados mediante representacion segura.
- regenerables.
- los anteriores quedan invalidados al regenerar.

# 4. Recuperacion de password - planeado

Flujo:

```text
usuario solicita recuperacion
        |
        v
backend genera token
        |
        v
correo
        |
        v
usuario define nueva password
```

Reglas:

- token temporal.
- token de un solo uso.
- guardar hash del token, no token original.
- invalidar token despues de uso.

# 5. Verificacion de correo - planeado

Las cuentas podran verificar la propiedad del correo registrado.

La verificacion utilizara un token temporal.

# 6. Refresh Tokens - planeado

Arquitectura objetivo:

```text
Access Token
vida corta

Refresh Token
vida mayor
revocable
```

Permitira renovar autenticacion sin mantener un access token excesivamente largo.

# 7. Sesiones - planeado

El usuario podra administrar sesiones activas.

Objetivo:

- ver dispositivos.
- cerrar una sesion.
- cerrar todas las sesiones.
- revocar refresh tokens.

# 8. Proteccion de login - planeado

Se incorporara:

- rate limiting.
- limite de intentos.
- bloqueo temporal.
- auditoria de intentos relevantes.

El bloqueo no debe permitir ataques simples de denegacion dirigidos contra cuentas de terceros.

# 9. Auditoria - planeado

Acciones administrativas importantes deberan registrarse.

Ejemplos:

- creacion de usuario.
- cambio de rol.
- cambio de estado.
- asignacion de sede.
- cambio de reserva.
- accion sobre pago.

La auditoria debe permitir identificar:

- quien.
- que accion.
- cuando.
- sobre que recurso.

## Pagos base - seguridad actual

FASE 7.6F aplica autorizacion en dos niveles:

1. Spring Security.
2. validacion de propiedad o alcance dentro del Service.

CLIENTE:

```text
POST /api/reservas/{idReserva}/pagos
GET  /api/reservas/{idReserva}/pagos
GET  /api/pagos/{idPago}
```

Reglas:

- el Cliente se obtiene desde el correo autenticado.
- el frontend no envia `idCliente`.
- la Reserva se consulta mediante `idReserva + idCliente`.
- el Pago individual se consulta mediante `idPago + idCliente`.
- un recurso perteneciente a otro Cliente responde 404.

BackOffice:

```text
GET /api/backoffice/pagos
GET /api/backoffice/pagos/{idPago}
```

ADMINISTRADOR:

- alcance global.

ENCARGADO_LOCAL:

- la sede se obtiene desde `UsuarioSede`.
- el frontend no envia `idSede`.
- los listados filtran por `Reserva.sede.idSede`.
- el detalle consulta mediante `idPago + idSede`.
- un Pago de otra sede responde 404.

CLIENTE no tiene acceso a `/api/backoffice/pagos/**`.

Desde FASE 7.6G no existe un endpoint tecnico de transicion de Pago.

`GET /api/pagos/{idPago}` permanece disponible exclusivamente para que un CLIENTE consulte un Pago propio.

Las reglas de transicion permanecen encapsuladas en el backend.
# 10. Pagos - seguridad futura

La integracion con Mercado Pago debera:

- validar webhooks.
- verificar autenticidad.
- utilizar idempotencia.
- evitar procesar eventos duplicados.
- no confiar en datos enviados solamente por el navegador.

# 11. IA

La IA no posee privilegios especiales.

La IA nunca puede:

- acceder directamente a SQL Server.
- saltarse autorizacion.
- aprobar pagos.
- modificar estados ignorando reglas.
- definir permisos.

Toda accion debe atravesar los mismos Services que utilizarian otros consumidores.

# 12. Frontend

Ocultar un boton no constituye seguridad.

Aunque ASP.NET MVC no muestre una opcion, el backend debe impedir la operacion si el usuario no esta autorizado.

# 13. Principio de minimo privilegio

Cada rol debe acceder solamente a lo necesario.

Ejemplo:

ENCARGADO_LOCAL no debe poder acceder a recursos administrativos de otras sedes.

# 14. Reglas de repositorio

Antes de un commit comprobar que no se incluyan:

- .env.
- environments locales de Postman con secretos.
- passwords.
- tokens.
- archivos de backup local.

# 15. Estado actual

IMPLEMENTADO:

- BCrypt.
- JWT.
- Spring Security.
- roles.
- registro publico restringido a CLIENTE.
- perfil Cliente sincronizado con el rol CLIENTE.
- acceso administrativo por rol.
- restriccion de sede en BackOffice de sedes.
- restriccion de sede en Reservas BackOffice.

PENDIENTE:

- aplicar alcance por sede a Pagos reales.
- MFA.
- recovery codes.
- recuperacion de password.
- verificacion de correo.
- refresh tokens.
- sesiones.
- rate limiting.
- auditoria completa.
- seguridad de proveedor de pagos.
