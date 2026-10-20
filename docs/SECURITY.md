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

# 2. MFA - implementada
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

# 3. Codigos de recuperacion - implementacion parcial

Al activar MFA se generaran codigos de recuperacion.

Reglas:

- uso unico.
- almacenados mediante representacion segura.
- regenerables.
- los anteriores quedan invalidados al regenerar.

# 4. Recuperacion de password - implementado

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

# 5. Verificacion de correo - implementado

Las cuentas podran verificar la propiedad del correo registrado.

La verificacion utilizara un token temporal.

# 6. Refresh Tokens - implementado

Arquitectura objetivo:

```text
Access Token
vida corta

Refresh Token
vida mayor
revocable
```

Permitira renovar autenticacion sin mantener un access token excesivamente largo.

# 7. Sesiones - implementado

El usuario podra administrar sesiones activas.

Objetivo:

- ver dispositivos.
- cerrar una sesion.
- cerrar todas las sesiones.
- revocar refresh tokens.

# 8. Proteccion de login - implementada parcialmente

Se incorporara:

- rate limiting.
- limite de intentos.
- bloqueo temporal.
- auditoria de intentos relevantes.

El bloqueo no debe permitir ataques simples de denegacion dirigidos contra cuentas de terceros.

# 9. Auditoria - implementada
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

- alcance por sede aplicado a Pagos BackOffice.
- MFA.
- recovery codes.
- recuperacion de password.
- verificacion de correo.
- refresh tokens.
- sesiones.
- rate limiting.
- auditoria completa.
- seguridad de proveedor de pagos.
## Defensa en profundidad BackOffice

La seguridad administrativa utiliza dos capas complementarias:

1. `SecurityConfig` restringe `/api/backoffice/**` a `ADMINISTRADOR` y `ENCARGADO_LOCAL`.
2. Los Controllers BackOffice conservan `@PreAuthorize`.

Adicionalmente:

- `/api/usuarios/**` esta restringido a `ADMINISTRADOR`.
- `ADMINISTRADOR` dispone de alcance global.
- `ENCARGADO_LOCAL` obtiene su sede autorizada desde `UsuarioSede`.
- el frontend no puede ampliar el alcance enviando otro `idSede`.
- Sedes, Reservas y Pagos pertenecientes a otra sede no quedan visibles para `ENCARGADO_LOCAL`.
- cuando corresponde proteger la existencia del recurso se responde 404 en lugar de 403.

## Cierre tecnico FASE 9.0.5

- Tokens de recuperacion y verificacion almacenados solo como SHA-256.
- Tokens opacos de un solo uso con expiracion.
- Tokens anteriores activos se revocan al emitir uno nuevo.
- Password reset usa PasswordEncoder con BCrypt.
- Password reset revoca sesiones activas.
- Password reset limpia estado de bloqueo de login.
- Solicitudes publicas usan respuestas genericas anti-enumeration.
- Entrega de correo abstraida mediante SecurityEmailService.
- Provider SMTP implementado mediante JavaMailSender.
- Mailpit v1.31.5 se utiliza exclusivamente como SMTP local de desarrollo.
- Nuevos usuarios se crean inicialmente con correo no verificado.
- Los usuarios preexistentes permanecen verificados.
- El enforcement de correo existe y fue validado end-to-end.
- El Docker estable mantiene enforcement deshabilitado hasta la integracion MVC de FASE 9.0.9.
- Los raw tokens no se imprimen ni se persisten.

## Rate limiting - FASE 9.0.6

Implementado para endpoints publicos sensibles:

- login;
- solicitud de recuperacion de password;
- reenvio de verificacion de correo.

Caracteristicas:

- fixed window in-memory;
- contador concurrente;
- clave por scope + SHA-256 del subject;
- identidad HTTP inicial mediante getRemoteAddr();
- no se confia en X-Forwarded-For;
- no se confia en X-Real-IP;
- respuesta 429 con Retry-After;
- auditoria RATE_LIMIT_EXCEDIDO;
- solo el primer rechazo por bucket y ventana genera auditoria;
- rate limiting y bloqueo persistente de login son controles independientes.

Limitacion:

La implementacion es local por instancia. Un despliegue multi-instancia requiere almacenamiento compartido, por ejemplo Redis o equivalente.

## Estado MFA validado - FASE 9.0.7C

Implementado:

- TOTP RFC 6238 con HMAC-SHA1;
- secreto TOTP aleatorio de 160 bits;
- periodo de 30 segundos;
- tolerancia de una ventana anterior y posterior;
- secreto TOTP protegido mediante AES-256-GCM;
- setup MFA autenticado mediante `POST /api/mfa/setup`;
- activacion MFA autenticada mediante `POST /api/mfa/activate`;
- recovery codes generados con aleatoriedad criptografica;
- solamente SHA-256 de recovery codes queda persistido;
- activacion atomica y protegida mediante lock pesimista.

Pendiente para FASE 9.0.7D:

- enforcement MFA durante login;
- challenge MFA temporal;
- validacion TOTP durante login;
- consumo one-time de recovery code durante login;
- rate limiting especifico para MFA login.

Mientras 9.0.7D no este completada, el login existente todavia no exige el segundo factor.

## Estado final MFA login - FASE 9.0.7D

IMPLEMENTADO:

- password correcto con MFA deshabilitado entrega JWT normal;
- password correcto con MFA habilitado entrega challenge temporal, no JWT;
- challenge opaco de 256 bits;
- challenge persistido solamente mediante SHA-256;
- un unico challenge activo por usuario y tipo;
- challenge de un solo uso;
- expiracion configurable, cinco minutos por defecto;
- verificacion TOTP durante login;
- recovery code como segundo factor alternativo;
- recovery codes de un solo uso mediante fechaUso;
- locks pesimistas para challenge, UsuarioSeguridad y recovery codes;
- MFA_VERIFICACION_EXITOSA;
- MFA_VERIFICACION_FALLIDA mediante REQUIRES_NEW;
- MFA_RECOVERY_CODE_USADO;
- rate limit MFA_CHALLENGE independiente del rate limit LOGIN;
- TOTP y recovery comparten el mismo presupuesto MFA_CHALLENGE.

La implementacion no entrega un access JWT antes de completar el segundo factor.

## Estado final Refresh Tokens + Sesiones - FASE 9.0.8

FASE 9.0.8 implementa el lifecycle backend de sesiones persistentes.

Refresh token:

- opaco y generado con aleatoriedad criptografica;
- solamente su hash SHA-256 se persiste;
- el valor raw se devuelve solamente al cliente autorizado;
- rota en cada uso exitoso;
- el token anterior deja de ser valido despues de la rotacion;
- la rotacion no extiende la expiracion absoluta de la sesion;
- tokens revocados o expirados son rechazados.

SesionUsuario:

- representa una sesion logica persistente;
- Spring Security permanece STATELESS;
- no se utiliza HttpSession para autenticar el backend;
- permite listar sesiones activas propias;
- permite revocar una sesion propia especifica;
- permite cerrar la sesion identificada por refresh token;
- permite revocar todas las sesiones activas del usuario.

Endpoints:

- `POST /api/auth/refresh`;
- `POST /api/auth/logout`;
- `POST /api/auth/logout-all`;
- `GET /api/auth/sessions`;
- `DELETE /api/auth/sessions/{idSesionUsuario}`.

Seguridad:

- `/api/auth/sessions` y `/api/auth/sessions/**` requieren autenticacion;
- `/api/auth/logout-all` requiere autenticacion;
- una sesion inexistente y una sesion perteneciente a otro usuario responden 404;
- `refreshTokenHash` nunca forma parte del DTO publico;
- no existe flag de sesion actual porque el access JWT no contiene sessionId/jti;
- revocar una sesion no invalida inmediatamente un access JWT ya emitido;
- el access JWT existente continua valido hasta su propia expiracion.

Auditoria:

- SESION_CREADA;
- SESION_RENOVADA;
- SESION_REVOCADA;
- TODAS_SESIONES_REVOCADAS.

## Estado FASE 9.0.9 - Integracion MVC completada

Actualizado: 2026-10-20.

El frontend ASP.NET Core MVC integra los controles de seguridad
implementados por Spring Boot sin trasladar la autoridad de seguridad
al navegador.

Implementado y validado:

- MFA TOTP durante login;
- recovery code como segundo factor;
- recuperacion de password;
- verificacion de correo;
- refresh tokens rotatorios;
- sesiones persistentes;
- logout con revocacion;
- cierre global de sesiones;
- administracion de sesiones propias;
- JWT y refresh token exclusivamente server-side en MVC;
- anti-enumeration en recuperacion y reenvio de verificacion;
- CSRF en acciones MVC que modifican estado.

Los estados historicos anteriores que describan estos controles como
"planeados" o "pendientes" quedan reemplazados por este estado para
FASE 9.0.9.

Pendiente para 9.0.10:

- smoke real de los flujos de seguridad;
- hardening del IdleTimeout MVC;
- hardening concurrency-safe del lifecycle de RefreshLocks;
- regresion completa por roles.

<!-- PHASE9_SECURITY_FINAL_START -->
## Estado final de seguridad - FASE 9

- Spring Boot permanece STATELESS respecto de HttpSession.
- Access JWT permanece valido hasta su expiracion natural.
- Revocar una sesion impide refresh posteriores; no existe jti/session-id coupling para revocacion inmediata del access JWT.
- Refresh tokens son opacos, se persisten solo mediante hash y rotan en cada refresh.
- True refresh reuse detection revoca las sesiones persistentes de la cuenta.
- MFA TOTP protege el secreto mediante AES-256-GCM.
- Recovery codes se persisten mediante SHA-256 y son one-time.
- Login con MFA no emite access JWT ni refresh antes de completar el segundo factor.
- TOTP y recovery comparten el scope MFA_CHALLENGE.
- Rate limiting devuelve HTTP 429 + Retry-After.
- RATE_LIMIT_EXCEDIDO audita solo el primer rechazo del bucket/ventana.
- Browser no recibe JWT ni refresh backend; ASP.NET MVC los mantiene server-side en Session.
- logout y logout-all revocan sesiones persistentes.
- CLIENTE, ENCARGADO_LOCAL y ADMINISTRADOR fueron validados en runtime.
- ENCARGADO_LOCAL obtiene HTTP 404 para sedes fuera de alcance como control anti-enumeration.
<!-- PHASE9_SECURITY_FINAL_END -->
