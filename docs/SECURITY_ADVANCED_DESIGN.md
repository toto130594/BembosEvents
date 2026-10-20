# Seguridad Avanzada - FASE 9

## 1. Objetivo

FASE 9 incorpora seguridad avanzada sobre la arquitectura existente de Bembos Events.

Alcance oficial:

- recuperacion de password;
- verificacion de correo;
- MFA TOTP;
- recovery codes;
- refresh tokens;
- sesiones;
- rate limiting;
- intentos fallidos;
- auditoria de seguridad.

La implementacion debe conservar las decisiones de arquitectura ya cerradas.

Spring Boot continua siendo la autoridad final de autenticacion, autorizacion y seguridad.

ASP.NET Core MVC continua actuando como cliente server-side del backend.

El navegador nunca recibe directamente los JWT ni los refresh tokens del backend.


## 2. Estado inicial confirmado en 9.0.1

La auditoria 9.0.1 determino:

- recuperacion de password: implementada en 9.0.5;
- verificacion de correo: implementada en 9.0.5;
- MFA TOTP: enrollment implementado en 9.0.7B/9.0.7C; login MFA pendiente en 9.0.7D;
- recovery codes: generacion y persistencia implementadas en 9.0.7C; consumo durante login pendiente en 9.0.7D;
- refresh tokens: no implementados;
- sesiones persistentes: no implementadas;
- rate limiting: implementado en 9.0.6;
- intentos fallidos: implementados en 9.0.4;
- auditoria de seguridad base: implementada desde 9.0.4.

La unica referencia a sesiones existente corresponde a:

Spring Security SessionCreationPolicy.STATELESS.

Esto no representa una sesion de usuario persistente.

Spring Boot debe continuar siendo stateless respecto de HttpSession.


## 3. Arquitectura objetivo

Flujo general:

Browser
  -> ASP.NET Core MVC
  -> BackendApiClient
  -> Spring Boot REST API
  -> Services de seguridad
  -> Repositories
  -> SQL Server

Reglas:

- browser nunca almacena access token;
- browser nunca almacena refresh token;
- ASP.NET Core conserva tokens solamente server-side;
- Spring Boot valida identidad y permisos;
- SQL Server almacena solamente hashes de tokens reutilizables;
- passwords nunca se almacenan en texto plano;
- codigos MFA nunca se almacenan en texto plano cuando puedan ser usados posteriormente;
- tokens sensibles nunca deben escribirse en logs;
- secretos TOTP nunca deben imprimirse en logs.


## 4. JWT actual

El access token JWT actual se conserva.

Su expiracion continua siendo configurable mediante:

app.jwt.expiration-ms

FASE 9 no convierte Spring Security a sesiones HttpSession.

El access token debe seguir siendo de vida limitada.

Cuando se implemente refresh token:

- JWT = autenticacion de requests;
- refresh token = renovacion del JWT;
- SesionUsuario = control server-side de la sesion logica.

La duracion exacta de access token y refresh token sera configurable.


## 5. Modelo de persistencia propuesto

FASE 9 necesitara persistencia adicional.

Las migraciones se implementaran mediante Flyway.

No se modificara manualmente la BD.


### 5.1 UsuarioSeguridad

Relacion uno a uno con Usuario.

Responsabilidades:

- estado de verificacion de correo;
- cantidad de intentos fallidos;
- fecha hasta la cual el usuario esta bloqueado;
- estado de MFA;
- secreto TOTP protegido;
- fechas de seguridad.

Campos conceptuales:

- idUsuario;
- correoVerificado;
- intentosFallidos;
- bloqueadoHasta;
- mfaHabilitado;
- totpSecretProtegido;
- fechaCreacion;
- fechaActualizacion.

No se debe duplicar passwordHash.

passwordHash continua perteneciendo a Usuario.


### 5.2 TokenSeguridadUsuario

Tokens de un solo uso.

Tipos iniciales:

- VERIFICACION_CORREO;
- RECUPERACION_PASSWORD.

Campos conceptuales:

- idTokenSeguridad;
- idUsuario;
- tipo;
- tokenHash;
- fechaCreacion;
- fechaExpiracion;
- fechaUso;
- fechaRevocacion.

Reglas:

- almacenar hash, nunca token original;
- token de un solo uso;
- token expirado no es valido;
- token usado no es valido;
- token revocado no es valido;
- crear un token nuevo puede revocar tokens previos activos del mismo tipo.


### 5.3 SesionUsuario

Representa una sesion logica del backend.

No es HttpSession.

Campos conceptuales:

- idSesionUsuario;
- idUsuario;
- refreshTokenHash;
- fechaCreacion;
- fechaExpiracion;
- fechaUltimoUso;
- fechaRevocacion;
- userAgent opcional;
- ipOrigen opcional.

Reglas:

- refresh token original nunca se persiste;
- solamente se almacena su hash;
- refresh token debe rotarse al utilizarse;
- logout revoca la sesion;
- cambio de password revoca sesiones activas;
- reutilizacion de un refresh token ya rotado debe considerarse sospechosa;
- el usuario puede cerrar una sesion o todas sus sesiones.


### 5.4 MfaRecoveryCode

Codigos de recuperacion MFA.

Campos conceptuales:

- idRecoveryCode;
- idUsuario;
- codigoHash;
- fechaCreacion;
- fechaUso.

Reglas:

- codigo original se entrega solamente al generarlo;
- BD almacena solamente hash;
- cada codigo puede utilizarse una sola vez;
- regenerar codigos invalida los anteriores.


### 5.5 AuditoriaSeguridad

Registro de eventos relevantes.

Campos conceptuales:

- idAuditoriaSeguridad;
- idUsuario nullable;
- tipoEvento;
- exitoso;
- fechaEvento;
- ipOrigen nullable;
- userAgent nullable;
- detalleSeguro nullable.

Nunca almacenar:

- passwords;
- JWT;
- refresh tokens;
- tokens de reset;
- tokens de verificacion;
- secretos TOTP;
- codigos MFA;
- recovery codes originales.


## 6. Eventos de auditoria

Eventos iniciales:

- LOGIN_EXITOSO;
- LOGIN_FALLIDO;
- USUARIO_BLOQUEADO;
- USUARIO_DESBLOQUEADO;
- PASSWORD_RESET_SOLICITADO;
- PASSWORD_RESET_COMPLETADO;
- CORREO_VERIFICADO;
- VERIFICACION_CORREO_REENVIADA;
- MFA_SETUP_INICIADO;
- MFA_ACTIVADO;
- MFA_DESACTIVADO;
- MFA_VERIFICACION_EXITOSA;
- MFA_VERIFICACION_FALLIDA;
- MFA_RECOVERY_CODE_USADO;
- SESION_CREADA;
- SESION_RENOVADA;
- SESION_REVOCADA;
- TODAS_SESIONES_REVOCADAS;
- RATE_LIMIT_EXCEDIDO.

La auditoria no reemplaza HistorialReserva.

HistorialReserva sigue perteneciendo al dominio de reservas.


## 7. Recuperacion de password

Flujo objetivo:

1. usuario indica correo;
2. backend responde de forma generica;
3. no se revela si el correo existe;
4. si existe usuario activo, se genera token aleatorio criptograficamente seguro;
5. solamente su hash se persiste;
6. token posee expiracion;
7. enlace es enviado mediante abstraccion de correo;
8. usuario envia token y nueva password;
9. backend valida hash, expiracion, uso y revocacion;
10. backend aplica PasswordEncoder;
11. token queda usado;
12. sesiones activas del usuario son revocadas;
13. se registra auditoria.

Respuesta de solicitud debe evitar enumeracion de usuarios.

Ejemplo conceptual:

"Si el correo corresponde a una cuenta valida, se enviaran instrucciones."


## 8. Verificacion de correo

Usuarios existentes antes de FASE 9 no deben quedar bloqueados accidentalmente.

Estrategia de migracion:

- usuarios existentes se consideran verificados inicialmente;
- usuarios nuevos creados despues de activar el flujo nacen sin correo verificado.

Flujo:

1. registro;
2. crear Usuario;
3. crear estado UsuarioSeguridad;
4. generar token de verificacion;
5. enviar correo;
6. usuario confirma;
7. backend marca correo como verificado;
8. token queda consumido;
9. auditoria registra el evento.

La politica exacta de operaciones permitidas a un usuario no verificado se definira antes de activar el enforcement.

No se debe romper el login actual sin una migracion controlada.


## 9. Intentos fallidos y bloqueo

Los intentos fallidos deben persistirse.

No depender exclusivamente de memoria.

Comportamiento objetivo:

- login correcto reinicia contador;
- password incorrecta incrementa contador;
- al superar limite configurable se establece bloqueadoHasta;
- una cuenta bloqueada no autentica aunque la password sea correcta;
- terminado el periodo de bloqueo puede volver a intentar;
- login de correo inexistente mantiene respuesta equivalente para evitar enumeracion.

Parametros configurables:

- numero maximo de intentos;
- duracion del bloqueo.

No se hardcodearan valores de produccion dentro del servicio.


## 10. Rate limiting

Rate limiting protege principalmente endpoints publicos sensibles.

Objetivos iniciales:

- login;
- solicitud de recuperacion de password;
- reenvio de verificacion;
- validacion MFA;
- refresh token.

Debe distinguirse:

Intentos fallidos:
  proteccion persistente de una cuenta.

Rate limiting:
  proteccion de frecuencia por origen y/o identidad.

FASE 9 utilizara una abstraccion:

AuthRateLimitService

La implementacion inicial puede utilizar memoria del proceso para el despliegue actual de una sola instancia.

La abstraccion debe permitir migrar posteriormente a un almacenamiento distribuido sin cambiar controllers.

El bloqueo por intentos fallidos continuara persistido en SQL Server.

HTTP esperado cuando se supera el limite:

429 Too Many Requests.


## 11. MFA TOTP

Se utilizara TOTP.

Flujo de activacion:

1. usuario autenticado solicita setup;
2. backend genera secreto;
3. backend entrega datos necesarios para configurar autenticador;
4. MFA aun no queda activo;
5. usuario envia primer codigo TOTP;
6. backend verifica codigo;
7. solamente entonces activa MFA;
8. se generan recovery codes;
9. se registra auditoria.

El secreto TOTP debe almacenarse protegido.

No debe guardarse como texto plano sin proteccion criptografica.

La clave de proteccion:

- proviene de configuracion externa;
- no se versiona;
- no se imprime.


## 12. Login con MFA

Cuando MFA esta deshabilitado:

correo + password
  -> access token
  -> refresh session

Cuando MFA esta habilitado:

correo + password
  -> challenge temporal
  -> codigo TOTP o recovery code
  -> access token
  -> refresh session

Nunca se entrega access token completo antes de completar MFA.

El challenge MFA:

- debe ser temporal;
- debe expirar;
- debe ser de un solo uso;
- no concede acceso a endpoints protegidos.


## 13. Recovery codes

Al activar MFA:

- generar conjunto de codigos aleatorios;
- mostrar una sola vez;
- almacenar solamente hashes.

Cada codigo:

- uso unico;
- queda marcado como utilizado;
- no puede recuperarse en texto plano desde BD.

El usuario puede regenerar codigos.

Regenerar:

- invalida todos los anteriores;
- genera nuevo conjunto;
- registra auditoria.


## 14. Refresh tokens

El refresh token sera opaco y aleatorio.

No sera un JWT con privilegios reutilizables.

Reglas:

- alta entropia;
- solamente hash en SQL Server;
- asociado a SesionUsuario;
- expiracion configurable;
- rotacion obligatoria;
- revocacion soportada;
- deteccion de reutilizacion;
- revocacion al cambiar password;
- revocacion al cerrar sesion.

Endpoint conceptual:

POST /api/auth/refresh

Request:

- refreshToken.

Response:

- nuevo access token;
- nuevo refresh token;
- expiraciones.


## 15. Sesiones

Las sesiones de FASE 9 son sesiones logicas persistidas.

No deben confundirse con:

- HttpSession de Spring;
- Session de ASP.NET Core.

Spring Security continua con:

SessionCreationPolicy.STATELESS.

Operaciones previstas:

- listar sesiones propias;
- revocar sesion actual;
- revocar una sesion propia;
- revocar todas las sesiones propias.

Un usuario nunca puede administrar sesiones de otro usuario mediante endpoints de cliente.


## 16. Integracion ASP.NET Core MVC

El navegador continua sin conocer tokens backend.

ASP.NET Core Session almacenara server-side:

- BackendJwt;
- BackendJwtExpiresAtUtc;
- BackendRefreshToken;
- BackendRefreshTokenExpiresAtUtc;
- BackendSessionId si resulta necesario.

Flujo de renovacion:

1. BackendBearerHandler detecta access token proximo a expirar o respuesta 401 elegible;
2. MVC solicita refresh server-side;
3. Spring rota refresh token;
4. MVC reemplaza tokens en Session;
5. request original puede reintentarse una sola vez.

Debe evitarse:

- bucle infinito de refresh;
- multiples refresh concurrentes de la misma sesion;
- refresh para endpoints /api/auth/login;
- exponer refresh token al browser.


## 17. Logout

Logout MVC objetivo:

1. solicitar revocacion de la sesion backend si existe;
2. limpiar BackendJwt;
3. limpiar BackendRefreshToken;
4. limpiar identificadores de sesion backend;
5. limpiar Session ASP.NET;
6. cerrar CookieAuthentication.

Aunque la revocacion backend falle por indisponibilidad, MVC debe eliminar credenciales locales.


## 18. Cambio de password

Al completar un reset de password:

- actualizar passwordHash con PasswordEncoder;
- revocar tokens de recuperacion activos;
- revocar sesiones activas;
- registrar auditoria.

No se modifica el password mediante SQL desde frontend.


## 19. Correo

Se introducira una abstraccion de envio.

Conceptualmente:

EmailService

Responsabilidades:

- enviar verificacion;
- enviar recuperacion de password;
- no decidir reglas de seguridad.

El proveedor concreto no debe acoplar AuthService a un proveedor externo.

Credenciales de correo:

- variables de entorno;
- nunca versionadas;
- nunca impresas.

FASE 9 puede utilizar una implementacion de desarrollo distinta de produccion.


## 20. Endpoints objetivo

Los nombres exactos se validaran contra REST_CONTRACT antes de implementarlos.

Familias requeridas:

Autenticacion:

- login;
- refresh;
- logout/revocacion.

Password:

- solicitar recuperacion;
- restablecer password.

Correo:

- verificar;
- reenviar verificacion.

MFA:

- setup;
- activar;
- verificar challenge;
- desactivar;
- regenerar recovery codes.

Sesiones:

- listar propias;
- revocar propia;
- revocar todas.

No se expondra un endpoint que permita:

- definir rol desde browser;
- definir idUsuario arbitrario;
- definir si MFA esta validado;
- definir correoVerificado;
- modificar contador de intentos;
- fabricar una sesion.


## 21. Respuestas y anti-enumeration

Operaciones publicas sensibles deben evitar revelar existencia de cuentas.

Aplicar especialmente a:

- recuperacion de password;
- reenvio de verificacion;
- login cuando sea posible sin destruir diagnostico interno.

Los logs internos pueden distinguir causas.

La respuesta publica no debe hacerlo cuando facilite enumeracion.


## 22. Configuracion

Toda politica debe ser configurable.

Categorias:

- access token TTL;
- refresh token TTL;
- password reset TTL;
- email verification TTL;
- MFA challenge TTL;
- maximos intentos fallidos;
- duracion de bloqueo;
- rate limits;
- issuer/configuracion TOTP;
- clave de proteccion del secreto TOTP.

Secretos nunca deben tener valores reales versionados.


## 23. Reglas de migracion

Flyway sera la unica fuente de cambios estructurales de BD.

No se permitira:

- ddl-auto update;
- ALTER manual como solucion permanente;
- SQL ejecutado por frontend.

Las migraciones deben:

- ser incrementales;
- ser reproducibles;
- preservar usuarios actuales;
- no invalidar automaticamente cuentas DEV existentes.


## 24. Compatibilidad con usuarios existentes

Los usuarios actuales fueron creados antes de FASE 9.

La introduccion de seguridad avanzada debe ser compatible.

Reglas iniciales:

- usuarios existentes permanecen activos;
- usuarios existentes se consideran con correo verificado durante migracion;
- MFA inicia deshabilitado;
- intentos fallidos inicia en cero;
- bloqueadoHasta inicia null;
- no existen sesiones persistentes previas;
- login actual continuara funcionando mientras se incorporan subfases.

Esto permite implementar FASE 9 incrementalmente.


## 25. Seguridad criptografica

Tokens aleatorios:

- SecureRandom;
- entropia suficiente;
- codificacion segura para URL cuando corresponda.

Persistencia:

- tokens reutilizables se almacenan mediante hash criptografico;
- recovery codes se almacenan mediante hash;
- passwords continuan mediante PasswordEncoder;
- secreto TOTP se almacena cifrado/protegido, no hasheado, porque debe recuperarse para validar TOTP.

La clave usada para proteger secreto TOTP no pertenece a la BD.


## 26. Logging

Nunca loggear:

- password;
- passwordHash;
- JWT;
- refresh token;
- reset token;
- verification token;
- TOTP secret;
- TOTP code;
- recovery code.

Logging permitido:

- idUsuario;
- tipo de evento;
- resultado;
- fecha;
- identificadores tecnicos no secretos;
- errores sanitizados.


## 27. Limites de FASE 9

FASE 9 no incluye:

- OAuth social;
- login con Google;
- login con Microsoft;
- passkeys/WebAuthn;
- SSO empresarial;
- Redis obligatorio;
- Mercado Pago;
- Chat;
- IA.

Estos elementos requieren fases o decisiones separadas.


## 28. Orden de implementacion

### 9.0.1 - Auditoria

Completada.

Resultado:

- infraestructura avanzada no implementada;
- JWT stateless actual confirmado;
- esquema actual sin tablas de seguridad avanzada.


### 9.0.2 - Diseno Seguridad Avanzada

Documento actual.

No modifica runtime.


### 9.0.3 - Persistencia y primitivas de seguridad

Objetivos:

- Flyway;
- UsuarioSeguridad;
- TokenSeguridadUsuario;
- SesionUsuario;
- MfaRecoveryCode;
- AuditoriaSeguridad;
- repositories;
- configuracion;
- primitives para tokens y hashes.

No activar aun todos los flujos publicos.


### 9.0.4 - Auditoria + intentos fallidos

Objetivos:

- SecurityAuditService;
- LOGIN_EXITOSO;
- LOGIN_FALLIDO;
- contador persistente;
- bloqueo temporal;
- reset de contador en login correcto;
- tests de anti-enumeration y bloqueo.


### 9.0.5 - Recuperacion de password + verificacion de correo

Objetivos:

- tokens de un solo uso;
- expiracion;
- consumo;
- EmailService;
- reset password;
- revocacion de sesiones;
- verificacion de correo;
- reenvio;
- anti-enumeration.


### 9.0.6 - Rate limiting

Objetivos:

- AuthRateLimitService;
- login;
- password recovery;
- email verification resend;
- MFA verify;
- refresh;
- HTTP 429;
- configuracion.


### 9.0.7 - MFA TOTP + recovery codes

Objetivos:

- setup;
- activacion segura;
- challenge;
- login MFA;
- recovery codes;
- regeneracion;
- desactivacion;
- auditoria.


### 9.0.8 - Refresh tokens + sesiones

Objetivos:

- refresh token opaco;
- hash persistido;
- rotacion;
- reuse detection;
- revocacion;
- listar sesiones;
- cerrar sesion;
- cerrar todas.


### 9.0.9 - Integracion MVC

Objetivos:

- recuperar password;
- verificar correo;
- MFA;
- guardar refresh token server-side;
- refresh automatico controlado;
- logout con revocacion;
- UI de sesiones.

JWT y refresh token nunca llegan al browser.


### 9.0.10 - Smoke y hardening

Validar:

- login normal;
- login bloqueado;
- password reset;
- email verification;
- MFA;
- recovery code;
- refresh rotation;
- reuse detection;
- logout;
- revoke all;
- rate limiting;
- auditoria;
- CLIENTE;
- ENCARGADO_LOCAL;
- ADMINISTRADOR;
- regresion completa.


### 9.0.11 - Cierre FASE 9

Objetivos:

- backend validator;
- frontend build;
- Docker;
- smoke real;
- documentacion;
- ROADMAP;
- CHANGELOG;
- checkpoint commit;
- sin push automatico.


## 29. Principios no negociables

1. Spring Boot sigue siendo autoridad final.
2. Browser no recibe JWT backend.
3. Browser no recibe refresh token backend.
4. SQL Server no almacena tokens reutilizables en texto plano.
5. Passwords nunca se recuperan; solamente se reemplazan.
6. MFA secret no se imprime.
7. Recovery codes originales no pueden recuperarse desde BD.
8. Login y recuperacion evitan enumeracion.
9. Cambio de password revoca sesiones.
10. Refresh token rota.
11. Reuse de refresh token debe detectarse.
12. Spring continua STATELESS.
13. Flyway controla el esquema.
14. Frontend nunca accede directamente a SQL Server.
15. FASE 9 se implementa incrementalmente y con regresion en cada subfase.

## 30. Cierre 9.0.3

Persistencia y primitivas de seguridad implementadas.

Resultado:

- Flyway V8 aplicada correctamente;
- UsuarioSeguridad creada;
- TokenSeguridadUsuario creada;
- SesionUsuario creada;
- MfaRecoveryCode creada;
- AuditoriaSeguridad creada;
- backfill de usuarios existentes completado;
- usuarios existentes conservados como correo verificado;
- MFA inicia deshabilitado;
- intentos fallidos inicia en cero;
- SecureRandom de 256 bits para tokens opacos;
- tokens URL-safe sin padding;
- SHA-256 para persistencia de hashes;
- comparacion mediante MessageDigest.isEqual;
- repositories base creados;
- enums de seguridad creados;
- tests de SecurityTokenService validados;
- backend validator validado;
- datos de Reserva y Pago intactos;
- flujo Auth actual no modificado;
- JWT actual no modificado;
- Spring Security continua STATELESS;
- frontend no modificado.

FASE 9 todavia no activa:

- bloqueo por intentos fallidos;
- recuperacion de password;
- verificacion de correo para nuevos usuarios;
- rate limiting;
- MFA TOTP;
- recovery codes operativos;
- refresh tokens operativos;
- sesiones operativas;
- auditoria de login.

Siguiente subfase:

- 9.0.4 - Auditoria de seguridad + intentos fallidos.

## 31. Cierre 9.0.4

Auditoria de seguridad e intentos fallidos implementados.

Resultado:

- intentos fallidos persistidos en UsuarioSeguridad;
- limite de intentos configurable;
- duracion de bloqueo configurable;
- bloqueo temporal aplicado antes de AuthenticationManager;
- lock pesimista para actualizar estado de seguridad;
- login correcto reinicia contador;
- bloqueo expirado se limpia mediante el flujo normal;
- LOGIN_EXITOSO auditado;
- LOGIN_FALLIDO auditado;
- USUARIO_BLOQUEADO auditado;
- USUARIO_DESBLOQUEADO auditado;
- respuesta publica de bloqueo permanece generica;
- usuario inexistente no crea UsuarioSeguridad;
- password no se registra en logs;
- JWT no se imprime;
- smoke HTTP real validado;
- bloqueo y desbloqueo natural validados;
- Reserva y Pago permanecen intactos;
- Spring Security continua STATELESS;
- frontend no modificado.

La auditoria generada por el smoke se conserva como evidencia.

Siguiente subfase:

- 9.0.5 - Recuperacion de password + verificacion de correo.

## 32. Cierre 9.0.5

Recuperacion de password y verificacion de correo implementadas.

Resultado:

- SecurityTokenLifecycleService operativo;
- tokens opacos de 256 bits reutilizan SecurityTokenService;
- solo SHA-256 se persiste;
- expiracion, uso unico y revocacion validados;
- consumo protegido con lock pesimista;
- recuperacion de password anti-enumeration;
- reset utiliza BCrypt PasswordEncoder;
- sesiones activas se revocan tras reset;
- estado de bloqueo de login se limpia tras reset;
- verificacion de correo anti-enumeration;
- nuevos usuarios nacen con correoVerificado=false;
- los cinco usuarios preexistentes permanecen verificados;
- SecurityEmailService abstrae la entrega;
- SmtpSecurityEmailService implementa JavaMailSender;
- Mailpit v1.31.5 funciona como SMTP local de desarrollo;
- password recovery E2E validado;
- email verification E2E validada;
- enforcement de correo validado con backend temporal;
- Docker estable deja enforcement=false hasta FASE 9.0.9;
- raw tokens no se imprimen ni se persisten;
- tokens usados se preservan como evidencia;
- eventos de auditoria se preservan como evidencia;
- Reserva 1, Reserva 2 y Pago 1 permanecen intactos.

Siguiente subfase:

- 9.0.6 - Rate limiting.

## 33. Decision de implementacion 9.0.6B

La primera implementacion de rate limiting utiliza memoria del proceso.

Motivos:

- el despliegue actual utiliza una sola instancia backend;
- no se requiere Redis para la fase academica actual;
- no se modifica Flyway;
- la abstraccion permite reemplazar la estrategia mas adelante.

Algoritmo inicial:

- ventana fija;
- contador concurrente por scope + subject;
- actualizacion atomica mediante ConcurrentHashMap.compute;
- limpieza oportunista de ventanas expiradas;
- el subject no se conserva en texto plano dentro de la clave;
- la clave utiliza SHA-256 del subject.

Scopes iniciales:

- LOGIN;
- PASSWORD_RECOVERY;
- EMAIL_VERIFICATION.

Politicas por defecto:

- login: 10 solicitudes por 60 segundos;
- solicitud de recuperacion: 5 solicitudes por 900 segundos;
- reenvio de verificacion: 5 solicitudes por 900 segundos.

Todas las politicas son configurables mediante application.properties y variables de entorno.

Identidad HTTP inicial:

- se utiliza HttpServletRequest.getRemoteAddr();
- no se confia en X-Forwarded-For;
- no se confia en X-Real-IP;
- forwarded headers requieren una configuracion explicita de proxy confiable antes de poder utilizarse.

Limitaciones conocidas:

- los contadores son locales a cada instancia;
- un reinicio del backend reinicia los contadores;
- si el backend escala horizontalmente, debe migrarse a un almacenamiento compartido como Redis o equivalente.

La respuesta HTTP 429, Retry-After, auditoria RATE_LIMIT_EXCEDIDO e integracion con endpoints se implementan en 9.0.6C.

## 34. Integracion HTTP 9.0.6C

El rate limiter se integra mediante RateLimitFilter dentro de Spring Security.

Endpoints protegidos inicialmente:

- POST /api/auth/login;
- POST /api/auth/password/solicitar;
- POST /api/auth/correo/reenviar.

No se limita en esta subfase:

- registro;
- consumo de token de recuperacion;
- consumo de token de verificacion;
- catalogos publicos;
- endpoints autenticados.

Cuando se supera el limite:

- HTTP 429 Too Many Requests;
- header Retry-After con segundos restantes de la ventana;
- cuerpo ApiErrorResponse con mensaje publico generico;
- el request no llega al controller.

Auditoria:

- utiliza RATE_LIMIT_EXCEDIDO;
- solo se registra el primer rechazo de cada bucket y ventana;
- los rechazos posteriores de la misma ventana no generan escrituras adicionales;
- la IP raw no se guarda en detalleSeguro.

La identidad inicial sigue utilizando getRemoteAddr() y no confia en X-Forwarded-For ni X-Real-IP.

Limitacion conocida:

En la arquitectura MVC server-side, varias solicitudes pueden llegar al backend desde el mismo peer de red del frontend. Esta fase conserva getRemoteAddr() porque aun no existe una politica de proxies confiables. La estrategia de forwarding/trusted proxy debe revisarse durante la integracion MVC de 9.0.9 antes de un despliegue distribuido o expuesto a usuarios reales.

El rate limit permanece separado del contador persistente de intentos fallidos:

- un 429 no incrementa intentosFallidos;
- un 429 no aplica bloqueadoHasta;
- el bloqueo de login y el rate limiting son controles independientes.

## 35. Cierre 9.0.6

Rate limiting implementado y validado end-to-end.

Resultado:

- estrategia in-memory;
- algoritmo fixed window;
- actualizacion thread-safe mediante ConcurrentHashMap.compute;
- clave scope + SHA-256 del subject;
- limpieza oportunista de ventanas expiradas;
- login protegido;
- solicitud de recuperacion protegida;
- reenvio de verificacion protegido;
- HTTP 429 validado;
- Retry-After validado;
- ApiErrorResponse preservado;
- RATE_LIMIT_EXCEDIDO validado en BD real;
- un solo evento de auditoria por bucket y ventana;
- rechazos adicionales no amplifican escrituras de auditoria;
- 429 no incrementa intentosFallidos;
- 429 no aplica bloqueadoHasta;
- getRemoteAddr() permanece como identidad inicial;
- X-Forwarded-For y X-Real-IP no son confiados;
- configuracion normal restaurada despues del smoke;
- contadores in-memory del backend temporal descartados;
- Reserva 1, Reserva 2 y Pago 1 permanecen intactos;
- Flyway no modificado;
- frontend no modificado.

Evidencia runtime RATE_LIMIT_EXCEDIDO se preserva.

Siguiente subfase:

- 9.0.7 - MFA TOTP + recovery codes.

## 36. Decision criptografica MFA 9.0.7B

La base MFA utiliza TOTP compatible con RFC 6238.

Parametros iniciales:

- algoritmo HMAC-SHA1;
- secreto aleatorio de 160 bits;
- codigo de 6 digitos;
- periodo de 30 segundos;
- tolerancia de una ventana anterior y una posterior.

Se prepara un URI otpauth compatible con aplicaciones autenticadoras.

Proteccion del secreto TOTP:

- el secreto TOTP no debe almacenarse en texto plano;
- se protege mediante AES-256-GCM;
- cada cifrado utiliza un IV aleatorio de 96 bits;
- authentication tag de 128 bits;
- payload versionado;
- AAD especifico de Bembos Events MFA;
- la clave de cifrado se obtiene desde SECURITY_MFA_ENCRYPTION_KEY;
- la clave debe contener 32 bytes codificados en Base64;
- no existe una clave hardcoded ni un valor seguro por defecto;
- si la clave no esta configurada, las operaciones de proteccion fallan de forma cerrada.

Recovery codes:

- se generan mediante SecureRandom;
- ocho codigos por defecto;
- cada codigo utiliza al menos 80 bits de aleatoriedad;
- se muestran en claro solamente al usuario durante su generacion;
- en persistencia se almacena solamente SHA-256;
- la comparacion de hash utiliza MessageDigest.isEqual;
- los codigos son de un solo uso cuando se integren con MfaRecoveryCode en 9.0.7C.

Estado de esta subfase:

- no se habilita MFA para ningun usuario;
- no se escriben secretos en BD;
- no se generan recovery codes en BD;
- no se modifica login;
- no se exponen endpoints;
- no se modifica Docker;
- SECURITY_MFA_ENABLED permanece false por defecto.

La integracion con UsuarioSeguridad, MfaRecoveryCode, endpoints de setup/activacion y auditoria se realiza en 9.0.7C.

## 37. Estado MFA despues de 9.0.7C

FASE 9.0.7C completa el enrollment MFA.

Implementado:

- `POST /api/mfa/setup` autenticado;
- generacion del secreto TOTP;
- proteccion AES-256-GCM antes de persistencia;
- URI `otpauth`;
- activacion solamente despues de validar un TOTP correcto;
- ocho recovery codes por defecto;
- recovery codes raw mostrados solamente en la respuesta de activacion;
- persistencia exclusiva de SHA-256;
- preservacion del historial de recovery codes ya usados;
- invalidacion de recovery codes no usados pertenecientes a una configuracion anterior;
- `mfaHabilitado=true` solamente despues de almacenar correctamente los recovery codes;
- transacciones y lock pesimista para proteger enrollment concurrente;
- tests negativos y de seguridad;
- backend validator completo aprobado.

No forma parte de 9.0.7C:

- alterar el flujo normal de login;
- emitir un challenge MFA;
- consumir recovery codes durante login;
- aplicar rate limiting especifico al segundo factor;
- modificar JWT.

Por lo tanto, tener MFA activado en persistencia todavia no significa que el login exija MFA. Ese enforcement corresponde a 9.0.7D.

## 38. Diseno aprobado para 9.0.7D

El login MFA utilizara un estado intermedio antes de entregar el JWT completo.

Diseno:

1. correo y password se validan normalmente;
2. si MFA esta deshabilitado, se mantiene el JWT normal;
3. si MFA esta habilitado, no se entrega JWT completo;
4. se utilizara un challenge temporal opaco;
5. el challenge reutilizara la infraestructura de tokens de seguridad existente;
6. solamente el hash SHA-256 del challenge podra persistirse;
7. el challenge tendra expiracion corta y consumo one-time;
8. TOTP correcto o recovery code valido completaran el segundo factor;
9. solamente despues se emitira el JWT normal.

No se utilizara un pre-MFA JWT.

En el checkpoint de diseno la implementacion permanecia pendiente. Estado actual: COMPLETADA en FASE 9.0.7D. hasta completar 9.0.7D.

## 39. Implementacion final MFA login 9.0.7D

El diseno aprobado para login MFA fue implementado y validado.

Flujo final:

1. POST /api/auth/login autentica correo y password.
2. Si mfaHabilitado=false, se conserva el JWT normal.
3. Si mfaHabilitado=true, no se emite JWT.
4. Se genera MFA_LOGIN_CHALLENGE mediante la infraestructura TokenSeguridadUsuario.
5. El challenge raw se devuelve una vez al cliente.
6. En BD persiste solamente SHA-256.
7. El challenge tiene TTL de cinco minutos por defecto.
8. POST /api/auth/mfa/verificar valida TOTP.
9. POST /api/auth/mfa/recuperacion valida un recovery code no usado.
10. Solamente despues del segundo factor correcto se consume el challenge y se emite JWT.

Concurrencia y replay:

- TokenSeguridadUsuario se consulta mediante PESSIMISTIC_WRITE;
- UsuarioSeguridad se bloquea mediante PESSIMISTIC_WRITE;
- recovery codes no usados se bloquean mediante PESSIMISTIC_WRITE;
- challenge consumido no puede reutilizarse;
- recovery code con fechaUso no puede reutilizarse.

Auditoria:

- MFA_VERIFICACION_EXITOSA en verificaciones exitosas;
- MFA_VERIFICACION_FALLIDA en verificaciones fallidas;
- los fallos usan una transaccion REQUIRES_NEW;
- MFA_RECOVERY_CODE_USADO registra consumo recovery;
- los secretos raw no forman parte del detalle de auditoria.

Rate limiting:

- scope MFA_CHALLENGE;
- independiente del scope LOGIN;
- TOTP y recovery comparten el mismo contador MFA;
- cinco solicitudes por sesenta segundos por defecto;
- valores configurables mediante variables de entorno.

Flyway:

- V8 no fue modificado;
- V9 amplia CK_TokenSeguridadUsuario_tipo con MFA_LOGIN_CHALLENGE.

Validacion final 9.0.7D:

- tests: OK;
- Docker Build: OK;
- scripts/validate-backend.ps1: OK;
- git diff --check: OK.

## 40. Implementacion final 9.0.8

FASE 9.0.8 - Refresh tokens + sesiones fue implementada y validada.

Objetivos completados:

- refresh token opaco;
- hash SHA-256 persistido;
- rotacion;
- reutilizacion del token anterior rechazada;
- revocacion;
- listar sesiones propias;
- cerrar sesion;
- cerrar todas las sesiones.

Lifecycle implementado:

```text
LOGIN MFA OFF
  -> access JWT
  -> refresh token
  -> SesionUsuario

LOGIN MFA ON
  -> challenge
  -> TOTP/recovery correcto
  -> access JWT
  -> refresh token
  -> SesionUsuario

REFRESH
  -> hash del token recibido
  -> PESSIMISTIC_WRITE
  -> validar activa/no expirada
  -> rotar hash
  -> conservar expiracion absoluta
  -> nuevo access JWT
  -> nuevo refresh token

LOGOUT
  -> revocar SesionUsuario asociada al refresh token

SESSION MANAGEMENT
  -> GET /api/auth/sessions
  -> DELETE /api/auth/sessions/{idSesionUsuario}
  -> POST /api/auth/logout-all
```

Reglas finales:

- Spring Boot continua STATELESS;
- `SesionUsuario` no es HttpSession;
- raw refresh token nunca se persiste;
- hash de refresh nunca se expone;
- sesiones ajenas no pueden enumerarse mediante id;
- query de revocacion especifica combina idSesionUsuario + idUsuario;
- revocacion especifica usa PESSIMISTIC_WRITE;
- sesiones revocadas y expiradas no aparecen en el listado activo;
- no existe sessionId/jti dentro del access JWT;
- revocacion de la sesion no invalida inmediatamente access JWT ya emitidos;
- access JWT permanece valido hasta su expiracion.

Estado:

- backend validator: OK;
- tests: OK;
- Docker build: OK;
- frontend: no modificado en 9.0.8;
- siguiente subfase: 9.0.9 - Integracion MVC.

## Cierre tecnico 9.0.9 - Integracion MVC - 2026-10-20

La integracion MVC de seguridad avanzada queda completada.

Arquitectura resultante:

Browser
  |
  | ASP.NET auth/session cookies
  v
ASP.NET Core MVC
  |
  | JWT backend + refresh token
  | almacenados server-side
  v
Spring Boot REST API
  |
  v
SQL Server

Reglas verificadas:

- el browser no recibe JWT backend;
- el browser no recibe refresh token backend;
- el challenge MFA permanece server-side;
- password correcto con MFA habilitado no crea sesion MVC antes del segundo factor;
- refresh es preemptivo y no reactivo a 401;
- no existe replay automatico del request original;
- logout actual revoca la sesion backend cuando existe refresh token;
- logout-all revoca sesiones backend y elimina el estado MVC local;
- password reset elimina autenticacion local tras completarse;
- sesiones administrables solamente mediante endpoints autenticados;
- no se intenta identificar la sesion actual mediante IP o User-Agent.

Hardening diferido a 9.0.10:

- revisar la politica de IdleTimeout de ASP.NET Session, actualmente 1 hora;
- implementar lifecycle seguro de RefreshLocks sin romper la exclusion mutua entre requests concurrentes.

9.0.9 no mantiene observaciones bloqueantes.

## 9.0.10A - Politica de sesion MVC y lifecycle de refresh locks

Actualizado: 2026-10-20.

### Politica MVC

Se mantiene intencionalmente:

- cookie de autenticacion MVC: 1 hora;
- SlidingExpiration: false;
- ASP.NET Session IdleTimeout: 1 hora.

El refresh token backend no se utiliza como mecanismo de
"recordarme por 30 dias" en el navegador.

Su objetivo en MVC es renovar de forma server-side el access JWT
mientras la autenticacion MVC actual siga vigente.

Por lo tanto, el TTL largo del refresh token backend no implica
una cookie MVC de igual duracion.

### RefreshLocks

El diccionario de exclusiones por Session ID utiliza ahora entradas
con conteo de referencias.

Cada entrada contiene:

- SemaphoreSlim;
- contador de referencias;
- SyncRoot para coordinar altas y bajas del diccionario.

La entrada solamente puede eliminarse cuando su ReferenceCount
llega a cero y continua siendo la misma instancia registrada para
ese Session ID.

Esto evita el patron inseguro de remover simplemente el
SemaphoreSlim despues de Release(), lo cual podria permitir que
un request rezagado utilice el semaphore antiguo mientras otro
request crea uno nuevo para el mismo Session ID.

El lock es por proceso MVC. Una futura arquitectura multi-instancia
requerira revisar la coordinacion distribuida y el almacenamiento
de ASP.NET Session.
