# Changelog - Bembos Events

Los cambios relevantes del proyecto se registran aqui.

## 2026-10-16

### Seguridad y autenticacion

- Autenticacion JWT disponible.
- Passwords protegidos con BCrypt.
- Roles CLIENTE, ENCARGADO_LOCAL y ADMINISTRADOR.
- Registro publico limitado a CLIENTE.

### Administracion de usuarios

- CRUD administrativo de usuarios.
- Creacion de ENCARGADO_LOCAL con sede.
- Cambio de rol.
- Cambio de estado.

### Alcance por sede

- Incorporada entidad UsuarioSede.
- ADMINISTRADOR con acceso global.
- ENCARGADO_LOCAL limitado a su sede en BackOffice de sedes.
- Acceso a otra sede bloqueado con 403.

### Base de datos

- Flyway mantiene la evolucion del esquema.
- Roles base incorporados mediante migracion.
- UsuarioSede incorporado mediante migracion.

### Reservas

- Reglas de capacidad.
- disponibilidad.
- superposicion.
- monto minimo.
- deposito.
- estados.
- transiciones.

### Pagos

- Estados de pago definidos.
- Reglas de transicion disponibles.
- Endpoint tecnico validar-transicion identificado como temporal.

### Documentacion

- README sincronizado.
- CLAUDE.md actualizado.
- PROJECT_RULES.md creado.
- ARCHITECTURE.md creado.
- DECISIONS.md creado.
- BUSINESS_RULES.md creado.
- SECURITY.md creado.
- API.md creado.
- AI_CHAT.md creado.
- ROADMAP.md actualizado.
- Definition of Done documental establecida.

### Mejoras futuras aprobadas

- MFA mediante TOTP.
- recovery codes.
- recuperacion de password.
- verificacion de correo.
- refresh tokens.
- sesiones.
- rate limiting.
- auditoria.
- chat IA.
- escalamiento IA a encargado.
- Mercado Pago.
- webhooks seguros.
- idempotencia.
- eventos internos.
- IA avanzada.

### Siguiente

FASE 7.6 - Auditoria REST definitiva.


### Cierre FASE 7.5F

- FASE 7.5F cerrada correctamente.
- Documentacion sincronizada con el estado actual del proyecto.
- Definition of Done documental activada.
- README, reglas, arquitectura, API y seguridad sincronizados.
- FASE 7.6 pasa a ser la fase activa.

### FASE 7.6B - Contrato REST

- Contrato REST definitivo documentado.
- Endpoints existentes clasificados.
- Endpoints tecnicos marcados para retiro.
- Catalogos publicos definidos.
- Flujo real de reservas definido.
- Flujo BackOffice de reservas definido.
- Contrato base de pagos definido.
- Aprobacion de pagos reservada al flujo seguro del proveedor.
- Seguridad por sede integrada al contrato.
- REST_CONTRACT.md agregado.


### FASE 7.6C.1

- Catalogos publicos de lectura implementados.
- Sedes publicas.
- Salones por sede.
- Horarios por salon.
- Paquetes.
- Tematicas.
- Servicios adicionales.
- Solo registros activos son visibles publicamente.
- Recursos inexistentes o inactivos responden 404.
- Rutas GET publicas configuradas en Spring Security.


### FASE 7.6C.2

- Consulta publica de disponibilidad implementada.
- Se valida horario operativo del salon.
- Se reutiliza ReservaService para detectar solapamientos.
- Query parameters invalidos responden 400.


### FASE 7.6D.1 - Consistencia Usuario/Cliente

- El registro publico CLIENTE crea tambien su perfil Cliente.
- La creacion administrativa de CLIENTE crea su perfil Cliente.
- Cambiar un usuario a CLIENTE crea o reactiva su perfil.
- Cambiar un CLIENTE a otro rol conserva el perfil y lo desactiva.
- Cliente.idUsuario queda protegido mediante indice UNIQUE filtrado.
- Flyway V7 corrige CLIENTE existentes sin perfil.
- Incorporada dbo.ReservaCodigoSequence para codigos publicos de reserva.
- Flyway V7 validada fisicamente en SQL Server.
- Tests y Docker build validados.

### FASE 7.6D.2 - Crear reserva real del cliente

- Implementado POST /api/reservas para CLIENTE autenticado.
- El backend identifica el Cliente desde el correo autenticado.
- Sede, salon, capacidad y disponibilidad son validados por el backend.
- Paquete, tematica y servicios adicionales se validan contra catalogos activos.
- Los precios son obtenidos desde el backend y no desde el navegador.
- ReservaServicio conserva precioUnitario y subtotal historicos.
- El backend calcula monto base, servicios, total y deposito.
- Los codigos publicos utilizan el formato BEM-YYYY-NNNNNN.
- Se registra HistorialReserva al crear la reserva.
- Tests especificos y regresion completa validados.
- Docker build validado.

### FASE 7.6D.3 - Mis reservas del cliente

- Implementado GET /api/reservas/mias.
- Requiere CLIENTE autenticado.
- El Cliente se obtiene desde el correo de autenticacion.
- El navegador no envia idCliente.
- La consulta filtra exclusivamente por el Cliente autenticado.
- Las reservas se ordenan por fechaCreacion descendente.
- Un cliente sin reservas recibe 200 con una lista vacia.
- Incorporado ReservaResumenResponse para no exponer entidades JPA.
- Tests de Controller y Service validados.
- Regresion completa del backend validada.
- Docker build validado.

### FASE 7.6D.4 - Consultar reserva propia

- Implementado GET /api/reservas/{idReserva} para CLIENTE propietario.
- El Cliente se obtiene desde el correo autenticado.
- El frontend no envia idCliente.
- La busqueda utiliza conjuntamente idReserva e idCliente.
- No se utiliza findById(idReserva) como mecanismo de autorizacion.
- Una reserva inexistente se trata como recurso no encontrado.
- Una reserva de otro cliente se trata de la misma forma para no revelar su existencia.
- Incorporado ReservaDetalleResponse.
- Incorporado ReservaDetalleServicioResponse.
- El detalle conserva cantidad, precioUnitario y subtotal historicos de los servicios adicionales.
- Tests especificos validados.
- Regresion completa del backend validada.
- Docker build validado.
- El acceso de ENCARGADO_LOCAL y ADMINISTRADOR permanece pendiente para FASE 7.6E.

### FASE 7.6D.5 - Cancelacion de reserva propia

- Implementado POST /api/reservas/{idReserva}/cancelacion.
- Acceso exclusivo para CLIENTE propietario.
- El Cliente se obtiene desde el correo autenticado.
- El frontend no envia idCliente.
- La reserva se busca mediante idReserva + idCliente.
- Una reserva inexistente o ajena responde como recurso no encontrado.
- PENDIENTE puede pasar a CANCELADA.
- CONFIRMADA puede pasar a CANCELADA.
- Los demas estados no permiten cancelacion mediante esta operacion.
- La validacion reutiliza ReservaService.validarTransicionEstado.
- Una transicion no permitida responde 409 Conflict.
- Se actualizan estado y fechaActualizacion.
- Se registra HistorialReserva con Usuario responsable.
- No se modifican automaticamente pagos ni se ejecutan reembolsos.
- Tests dirigidos validados.
- Regresion completa del backend validada.
- Docker build validado.
- FASE 7.6D de reservas del cliente completada.
- Siguiente fase: 7.6E - Reservas BackOffice.
### FASE 7.6E - Reservas BackOffice

- Implementado GET /api/backoffice/reservas.
- Implementado GET /api/backoffice/reservas/{idReserva}.
- Implementado PATCH /api/backoffice/reservas/{idReserva}/estado.
- Acceso restringido a ADMINISTRADOR y ENCARGADO_LOCAL.
- ADMINISTRADOR dispone de alcance global.
- ENCARGADO_LOCAL obtiene su sede desde UsuarioSede.
- El listado de ENCARGADO_LOCAL consulta solamente reservas de su sede.
- El detalle de ENCARGADO_LOCAL utiliza idReserva + idSede.
- El cambio de estado de ENCARGADO_LOCAL utiliza idReserva + idSede.
- Una reserva perteneciente a otra sede no queda visible para ENCARGADO_LOCAL.
- El frontend no utiliza idSede como fuente de autorizacion.
- El cambio de estado recibe solamente nuevoEstado.
- El estado actual se obtiene desde la Reserva persistida.
- Las transiciones reutilizan ReservaService.validarTransicionEstado.
- Una transicion no permitida responde 409 Conflict.
- La modificacion de estado utiliza PESSIMISTIC_WRITE.
- Se actualiza fechaActualizacion.
- Se registra HistorialReserva.
- El Usuario autenticado queda registrado como responsable.
- Tests dirigidos de Reservas BackOffice validados.
- Regresion completa del backend validada.
- Docker build validado.
- Postman sincronizado con los endpoints de Reservas BackOffice.
- Agregadas pruebas Postman para CLIENTE, ADMINISTRADOR y ENCARGADO_LOCAL.
- Agregadas variables de reserva al environment example.
- FASE 7.6E cerrada tecnicamente y documentalmente.
- Siguiente subfase: 7.6F - Pagos base.
### FASE 7.6F - Pagos base

- Implementado `POST /api/reservas/{idReserva}/pagos` para CLIENTE propietario.
- Implementado `GET /api/reservas/{idReserva}/pagos` para listar Pagos de una Reserva propia.
- Implementado `GET /api/pagos/{idPago}` para consultar un Pago propio.
- Implementado `GET /api/backoffice/pagos` para ADMINISTRADOR y ENCARGADO_LOCAL.
- Implementado `GET /api/backoffice/pagos/{idPago}` para ADMINISTRADOR y ENCARGADO_LOCAL.
- El Cliente se resuelve desde el correo autenticado.
- El frontend no envia `idCliente` como fuente de autorizacion.
- Una Reserva inexistente o ajena responde como recurso no encontrado.
- Un Pago inexistente o ajeno responde como recurso no encontrado.
- Se permiten pagos parciales y multiples Pagos por Reserva.
- Todo Pago nuevo inicia en estado `PENDIENTE`.
- `referenciaExterna` y `fechaPago` permanecen nulos durante el inicio base del Pago.
- `fechaCreacion` se genera en backend.
- ADMINISTRADOR dispone de alcance global en Pagos BackOffice.
- ENCARGADO_LOCAL solamente consulta Pagos asociados a Reservas de su sede.
- El alcance de ENCARGADO_LOCAL se obtiene desde `UsuarioSede`.
- Un Pago perteneciente a otra sede responde 404 para ENCARGADO_LOCAL.
- El endpoint tecnico `POST /api/pagos/validar-transicion` permanece temporalmente y sera retirado en FASE 7.6G.
- No se implementa un endpoint generico para que el navegador marque un Pago como `APROBADO`.
- Mercado Pago permanece planificado para FASE 11.
- Tests dirigidos de Pagos Cliente validados.
- Tests dirigidos de Pagos BackOffice validados.
- Seguridad por Cliente y por sede validada.
- Regresion completa del backend validada.
- Docker build validado.
- Documentacion REST sincronizada.
- Postman sincronizado para CLIENTE, ADMINISTRADOR y ENCARGADO_LOCAL.
- Variables de Pagos agregadas al environment example.
- FASE 7.6F cerrada tecnicamente y documentalmente.
- Siguiente subfase: 7.6G - Retirar endpoints tecnicos.
### FASE 7.6G - Retiro de endpoints tecnicos

- Retirados los endpoints HTTP tecnicos de Reserva.
- Retirado el endpoint HTTP tecnico de transicion de Pago.
- Eliminados los Controllers utilizados exclusivamente por dichos endpoints.
- Eliminados los tests de Controller correspondientes a endpoints retirados.
- Eliminados DTOs tecnicos que quedaron sin referencias.
- `MensajeResponse` se conserva porque continua siendo utilizado por Auth.
- Se mantiene `ReservaService` con sus reglas internas de capacidad, disponibilidad, monto y transiciones.
- Se mantiene `PagoService.validarTransicionEstado`.
- Se mantienen `ReglasReserva`, `ReglasTransicionReserva` y `ReglasTransicionPago`.
- Los flujos REST definitivos de Cliente y BackOffice permanecen intactos.
- SecurityConfig conserva `GET /api/pagos/{idPago}` para CLIENTE y elimina la regla generica asociada al endpoint tecnico de Pago.
- Regresion completa del backend validada.
- Docker build validado.
- FASE 7.6G completada.
- Siguiente subfase: 7.6H - Documentacion, Postman y tests.
### FASE 7.6H - Documentacion, Postman y tests

- Documentacion REST sincronizada con la API definitiva.
- Eliminadas de API.md las secciones de endpoints tecnicos retirados.
- REST_CONTRACT.md conserva las reglas de Reserva y Pago como reglas internas.
- BUSINESS_RULES.md actualizado para reflejar validaciones internas.
- SECURITY.md actualizado tras retirar el endpoint tecnico de Pago.
- Postman limpiado de requests correspondientes a endpoints retirados.
- DATABASE_RULES.md sincronizado con la estructura real de Pago.
- `metodoPago` documentado como obligatorio.
- `fechaCreacion` documentada como obligatoria.
- `referenciaExterna` y `fechaPago` documentadas como opcionales.
- Se mantienen pagos parciales y multiples Pagos por Reserva.
- No se inventa un catalogo cerrado de metodos de pago.
- Mercado Pago permanece reservado para FASE 11.
- Regresion completa del backend validada.
- Tests OK.
- Docker Build OK.
- FASE 7.6H completada.
- Siguiente subfase: 7.6I - Validacion final y runtime smoke test.
### FASE 7.6I - Validacion final y runtime smoke

- Backend validado mediante `scripts/validate-backend.ps1`.
- Tests completos del backend: OK.
- Docker Build: OK.
- Imagen del backend reconstruida con el codigo definitivo de FASE 7.6.
- SQL Server 2022 verificado en estado healthy.
- Spring Boot iniciado correctamente en runtime.
- Flyway inicializado correctamente contra SQL Server.
- `GET /api/sedes` validado mediante HTTP real.
- `GET /api/servicios-adicionales` validado mediante HTTP real.
- No se detectaron errores graves de startup.
- Endpoints tecnicos de Reserva y Pago confirmados como retirados.
- Reglas internas de Reserva y Pago confirmadas como conservadas.
- Documentacion, reglas de base de datos y Postman sincronizados.
- FASE 7.6 - Auditoria REST completada.
- Siguiente fase: 7.7 - Seguridad completa por sede.
### FASE 7.7 - Seguridad completa por sede

- Agregada defensa en profundidad para `/api/backoffice/**` en `SecurityConfig`.
- `/api/backoffice/**` queda restringido a `ADMINISTRADOR` y `ENCARGADO_LOCAL`.
- `/api/usuarios/**` queda restringido a `ADMINISTRADOR`.
- Los Controllers BackOffice mantienen `@PreAuthorize`.
- `ADMINISTRADOR` conserva alcance global.
- `ENCARGADO_LOCAL` obtiene su sede exclusivamente desde `UsuarioSede`.
- El detalle de una Sede perteneciente a otro alcance responde 404.
- Las sedes inexistentes o inactivas responden 404.
- Reservas y Pagos BackOffice conservan su scope por sede previamente implementado.
- Agregados tests de `SedeBackofficeServiceImpl`.
- Regresion completa validada con 165 tests.
- Docker Build validado.
- Documentacion y Postman sincronizados con la politica territorial definitiva.
### FASE 7.7.6 - Runtime smoke y cierre

- Backend reconstruido con el codigo actual de FASE 7.7.
- Contenedor backend validado en estado RUNNING.
- SQL Server validado HEALTHY.
- API publica validada con HTTP 200.
- Login real validado para ADMINISTRADOR, ENCARGADO_LOCAL y CLIENTE.
- ADMINISTRADOR validado con alcance global.
- ENCARGADO_LOCAL validado con acceso HTTP 200 a su propia sede.
- Una sede real ajena al ENCARGADO_LOCAL responde HTTP 404.
- CLIENTE recibe HTTP 403 en recursos BackOffice.
- Usuario anonimo recibe HTTP 401 en recursos BackOffice.
- Reservas y Pagos BackOffice validados en runtime.
- `git diff --check` validado.
- FASE 7.7 cerrada tecnicamente, documentalmente y en runtime.

### FASE 8.0.12 - Cierre global de FASE 8

- Sincronizado ROADMAP con el estado real del proyecto.
- FASE 7.7 marcada COMPLETADA.
- FASE 8 marcada COMPLETADA.
- Catalogos frontend cerrados.
- Reservas frontend cerradas.
- Pagos frontend cerrados.
- BackOffice MVC cerrado.
- ADMINISTRADOR global validado.
- ENCARGADO_LOCAL con scope por sede validado.
- CLIENTE rechazado en BackOffice.
- Anti-enumeration validado.
- Backend validado con 165 tests.
- Frontend Release validado sin warnings C#.
- Docker frontend validado.
- No se modifico backend.
- No se modifico Flyway.
- Siguiente fase: FASE 9 - Seguridad avanzada.

### FASE 9.0.3 - Persistencia y primitivas de seguridad

- Agregada migracion Flyway V8 de seguridad avanzada.
- Agregada entidad UsuarioSeguridad.
- Agregada entidad TokenSeguridadUsuario.
- Agregada entidad SesionUsuario.
- Agregada entidad MfaRecoveryCode.
- Agregada entidad AuditoriaSeguridad.
- Agregados repositories de persistencia de seguridad.
- Agregados enums TipoTokenSeguridad y TipoEventoSeguridad.
- Usuarios existentes migrados como correo verificado.
- MFA inicia deshabilitado.
- Intentos fallidos inicia en cero.
- Agregado SecurityTokenService.
- Tokens opacos generados con SecureRandom de 256 bits.
- Persistencia preparada para hashes SHA-256.
- Comparacion de hashes mediante MessageDigest.isEqual.
- Agregados tests de SecurityTokenService.
- Backend validator validado.
- V8 aplicada correctamente en SQL Server.
- Datos existentes de reservas y pagos preservados.
- Flujo de autenticacion actual no modificado.
- JWT actual no modificado.
- Spring Security permanece stateless.
- Frontend no modificado.
- Siguiente fase: 9.0.4 - Auditoria + intentos fallidos.

### FASE 9.0.4 - Auditoria e intentos fallidos

- Agregado SecurityAuditService.
- Agregado LoginSecurityService.
- Agregado bloqueo temporal por intentos fallidos.
- Maximo de intentos configurable.
- Duracion de bloqueo configurable.
- Agregado lock pesimista de UsuarioSeguridad.
- Login correcto reinicia contador.
- Expiracion de bloqueo desbloquea por flujo normal.
- LOGIN_EXITOSO auditado.
- LOGIN_FALLIDO auditado.
- USUARIO_BLOQUEADO auditado.
- USUARIO_DESBLOQUEADO auditado.
- Registro actual inicializa UsuarioSeguridad.
- Respuesta publica permanece generica.
- Agregados tests unitarios de seguridad de login.
- Smoke HTTP real validado.
- Password y JWT no impresos durante smoke.
- Evidencia de auditoria del smoke preservada.
- Reserva 1, Reserva 2 y Pago 1 preservados.
- Flyway no modificado.
- Frontend no modificado.
- Siguiente fase: 9.0.5 - Recuperacion de password + verificacion de correo.

### FASE 9.0.5 - Password recovery y verificacion de correo

- Agregado SecurityTokenLifecycleService.
- Agregada abstraccion SecurityEmailService.
- Agregado provider SMTP mediante JavaMailSender.
- Agregado Mailpit v1.31.5 para desarrollo Docker.
- Agregados endpoints de recuperacion de password.
- Agregados endpoints de verificacion de correo.
- Agregado TTL configurable de tokens.
- Agregado consumo one-time con lock pesimista.
- Agregada revocacion de tokens anteriores.
- Password reset usa BCrypt y revoca sesiones.
- Password reset limpia bloqueo temporal de login.
- Nuevos usuarios quedan inicialmente no verificados.
- Usuarios existentes permanecen verificados.
- Agregado enforcement configurable de correo.
- Enforcement validado con backend temporal.
- Docker estable mantiene enforcement deshabilitado hasta FASE 9.0.9.
- Smoke SMTP Mailpit real validado.
- Anti-enumeration validada para password y correo.
- Tokens raw no se imprimen ni persisten.
- Evidencia de tokens usados y auditoria preservada.
- Reserva 1, Reserva 2 y Pago 1 preservados.
- Flyway no modificado.
- Frontend no modificado.
- Siguiente fase: 9.0.6 - Rate limiting.

### FASE 9.0.6 - Rate limiting

- Agregados scopes LOGIN, PASSWORD_RECOVERY y EMAIL_VERIFICATION.
- Agregada estrategia in-memory fixed window.
- Agregada clave protegida con SHA-256.
- Agregado cleanup de buckets expirados.
- Agregada identidad HTTP mediante getRemoteAddr().
- Agregado RateLimitFilter en Spring Security.
- Protegidos login, solicitud de recuperacion y reenvio de verificacion.
- Agregado HTTP 429 Too Many Requests.
- Agregado header Retry-After.
- Agregada auditoria RATE_LIMIT_EXCEDIDO.
- La auditoria se limita al primer rechazo de cada bucket y ventana.
- Validado que 429 no incrementa intentos fallidos.
- Validado que 429 no bloquea al usuario.
- X-Forwarded-For no es confiado.
- X-Real-IP no es confiado.
- Smoke runtime validado con limites temporales de 2 solicitudes.
- Configuracion normal restaurada despues del smoke.
- Flyway no modificado.
- Frontend no modificado.
- Siguiente subfase: 9.0.7 - MFA TOTP + recovery codes.

## 2026-10-19

### FASE 9.0.7B - MFA crypto + TOTP

- Implementadas primitivas TOTP compatibles con RFC 6238.
- HMAC-SHA1, 6 digitos, periodo de 30 segundos y ventana +/-1.
- Secretos TOTP aleatorios de 160 bits.
- Agregada proteccion AES-256-GCM para secretos TOTP.
- IV aleatorio de 96 bits y tag de autenticacion de 128 bits.
- Clave criptografica externa; no existe clave hardcoded.
- Recovery codes generados con SecureRandom.
- Recovery codes persistibles solamente mediante SHA-256.
- Comparacion de hash constant-time.
- Login y JWT no fueron modificados.

### FASE 9.0.7C - MFA enrollment

- Implementado `POST /api/mfa/setup`.
- Implementado `POST /api/mfa/activate`.
- Ambos endpoints requieren usuario autenticado.
- Setup persiste solamente el secreto TOTP protegido.
- MFA permanece deshabilitado hasta validar el primer TOTP.
- Activacion genera recovery codes y habilita MFA atomicamente.
- Recovery raw se devuelve solamente durante activacion.
- Recovery raw no queda persistido ni registrado en logs.
- Recovery codes usados historicamente se preservan.
- Recovery codes no usados de configuraciones anteriores se invalidan.
- Agregado lock pesimista para proteger enrollment concurrente.
- Corregido test de tampering AES-GCM para alterar bytes reales del payload.
- Backend validator completo aprobado.
- Login, JWT y frontend permanecieron sin cambios.

### FASE 9.0.7D - Estado inicial

- Auditoria de login MFA completada.
- Aprobado diseno sin pre-MFA JWT.
- Se reutilizara la infraestructura de tokens opacos para challenge MFA.
- El en el estado inicial de 9.0.7D el challenge MFA aun no estaba implementado.
- Flyway V9 todavia NO esta creado en este checkpoint.
- Consumo de recovery code durante login permanece pendiente.
- Rate limiting MFA permanece pendiente.

## 2026-10-19 - FASE 9.0.7D MFA login completada

Implementado:

- MFA_LOGIN_CHALLENGE como nuevo TipoTokenSeguridad;
- V9 para ampliar el CHECK constraint de TokenSeguridadUsuario;
- TTL configurable del challenge;
- login MFA ON devuelve challenge y no JWT;
- login MFA OFF conserva flujo JWT existente;
- POST /api/auth/mfa/verificar;
- POST /api/auth/mfa/recuperacion;
- TOTP validado antes de consumir challenge;
- recovery code one-time con fechaUso;
- locks pesimistas para operaciones sensibles;
- MFA_VERIFICACION_EXITOSA;
- MFA_VERIFICACION_FALLIDA mediante REQUIRES_NEW;
- MFA_RECOVERY_CODE_USADO;
- scope de rate limiting MFA_CHALLENGE;
- TOTP y recovery comparten el presupuesto MFA;
- tests backend y Docker Build validados.

No se modifico:

- implementacion interna de JwtService;
- JwtAuthenticationFilter;
- frontend;
- V8.

## 2026-10-19 - FASE 9.0.8 - Refresh tokens y sesiones

- Implementado refresh token opaco.
- Solamente SHA-256 del refresh token se persiste.
- Implementada `SesionUsuario` como sesion logica persistente.
- Implementada emision de access JWT + refresh despues de autenticacion completa.
- MFA habilitado no crea sesion antes de completar el segundo factor.
- Implementada rotacion one-time de refresh tokens.
- La rotacion conserva la expiracion absoluta original.
- Refresh revocado, expirado o reutilizado es rechazado.
- Implementado `POST /api/auth/refresh`.
- Implementado `POST /api/auth/logout`.
- Implementado `POST /api/auth/logout-all`.
- Implementado `GET /api/auth/sessions`.
- Implementado `DELETE /api/auth/sessions/{idSesionUsuario}`.
- Listado limitado a sesiones activas propias.
- Revocacion especifica utiliza ownership por usuario y PESSIMISTIC_WRITE.
- Sesion inexistente o ajena responde 404 para evitar enumeracion.
- `refreshTokenHash` no se expone en respuestas.
- Agregados eventos SESION_CREADA, SESION_RENOVADA, SESION_REVOCADA y TODAS_SESIONES_REVOCADAS al lifecycle.
- Access JWT no tiene revocacion inmediata por sesion.
- No se agrego sessionId/jti al JWT.
- Backend validator: OK.
- Tests: OK.
- Docker build: OK.
- Frontend no modificado.
- FASE 9 permanece EN PROGRESO.
- Siguiente subfase: 9.0.9 - Integracion MVC.

## 2026-10-20 - FASE 9.0.9 Integracion MVC completada

### Added

- integracion MVC de MFA TOTP y recovery code;
- almacenamiento server-side de refresh token;
- refresh automatico controlado;
- logout con revocacion backend;
- Password Recovery MVC;
- Email Verification MVC;
- UI de gestion de sesiones;
- cierre individual y global de sesiones;
- mensajes y rutas MVC de seguridad.

### Security

- JWT backend nunca llega al browser;
- refresh token backend nunca llega al browser;
- challenge MFA permanece server-side;
- password recovery y resend de correo mantienen anti-enumeration;
- acciones MVC mutables protegidas mediante antiforgery;
- no se realiza retry reactivo ante HTTP 401;
- no se realiza replay automatico del request;
- PagoController limpia JWT y refresh al invalidar auth;
- ReservaController limpia JWT y refresh al invalidar auth.

### Deferred to 9.0.10

- definir politica definitiva para ASP.NET Session IdleTimeout;
- implementar cleanup/lifecycle concurrency-safe de RefreshLocks.

### Validation

- frontend Release build: OK;
- 9.0.9D: VALIDADA;
- 9.0.9E: VALIDADA;
- 9.0.9F: VALIDADA;
- 9.0.9G: VALIDADA;
- 9.0.9H: APROBADA;
- observaciones bloqueantes: 0.

No se realizo commit ni push.

## 2026-10-20 - FASE 9.0.10A refresh concurrency hardening

### Security

- se clasifica como intencional la politica MVC de 1 hora;
- auth cookie: 1 hora y no sliding;
- ASP.NET Session IdleTimeout: 1 hora;
- el refresh token backend no funciona como remember-me del browser;
- RefreshLocks deja de mantener entradas indefinidamente;
- se agrega reference counting por Session ID;
- TryRemove solamente ocurre al llegar a cero referencias;
- se evita retirar el lock mientras existen owners o waiters registrados;
- se conserva refresh preemptivo;
- no se agrega retry reactivo ante 401;
- no se agrega replay automatico de requests.

Backend, Flyway y base de datos no fueron modificados.

<!-- PHASE9_CHANGELOG_FINAL_START -->
## 2026-10-20 - FASE 9 seguridad avanzada cerrada

- MFA TOTP + recovery codes completados.
- Refresh tokens opacos + rotacion completados.
- Persistent sessions completadas.
- True refresh reuse detection validada en runtime.
- Logout y logout-all validados.
- Integracion MVC de password reset, email verification, MFA y session UI completada.
- JWT y refresh backend permanecen server-side en ASP.NET Session.
- Rate limiting LOGIN y MFA validado con HTTP 429 + Retry-After.
- RATE_LIMIT_EXCEDIDO verificado directamente en SQL.
- CLIENTE, ADMINISTRADOR y ENCARGADO_LOCAL validados en runtime.
- Resource hiding cross-sede mediante HTTP 404 confirmado.
- Backend validator: Tests OK + Docker Build OK.
- Frontend Release build: 0 warnings + 0 errors.
- Backend runtime HTTP 200.
- Frontend Docker runtime HTTP 200.
- SQL Server healthy.
- Sin errores transaccionales/Hikari en regresion final.
- 9.0.10 Smoke + Hardening aprobado.
- 9.0.11 documentacion sincronizada y lista para checkpoint commit.
- Commit y push no ejecutados automaticamente.
<!-- PHASE9_CHANGELOG_FINAL_END -->

<!-- PHASE10_CHANGELOG_FINAL_START -->
## 2026-10-21 - Cierre FASE 10 Chat e IA

### Backend

- Persistencia Conversacion/MensajeChat con Flyway V11.
- REST Cliente con ownership.
- REST BackOffice con alcance ADMIN global y ENCARGADO_LOCAL por sede.
- WebSocket/STOMP realtime.
- ChatAiProvider con proveedor basico y OpenAI-compatible opcional.
- Persistencia de respuestas IA.
- Escalamiento BOT -> ESPERANDO_ENCARGADO -> EN_ATENCION.
- Mensajes humanos de ENCARGADO_LOCAL.
- Historial y notificaciones realtime.

### Frontend

- MVC Cliente para crear, listar y operar conversaciones.
- MVC BackOffice para cola pendiente, toma y respuesta.
- Polling same-origin de historial.
- JWT y refresh token backend permanecen server-side.

### Hardening

- STOMP CONNECT autenticado.
- SUBSCRIBE autorizado por ownership, rol y sede.
- SEND directo a broker y notificaciones bloqueado.
- SEND /app valida ownership del CLIENTE.
- limite de mensajes de 4000 caracteres.
- MVC anti-forgery verificado.
- chat DOM sin innerHTML.
- IA sin acceso directo a SQL Server.

### Validacion

- backend tests: OK.
- backend Docker build: OK.
- backend startup: OK.
- frontend Release build: OK.
- runtime security smokes: OK.

FASE 10 cerrada tecnicamente y documentalmente.
Checkpoint commit pendiente de autorizacion explicita.
Push automatico: NO.
<!-- PHASE10_CHANGELOG_FINAL_END -->

<!-- PHASE11_MERCADOPAGO_CHANGELOG_START -->
## 2026-10-21 - Cierre FASE 11 Mercado Pago

FASE 11 - Mercado Pago: COMPLETADA.

Incluye:

- Checkout Pro mediante Orders API;
- configuracion TEST;
- provider server-side;
- checkout CLIENTE;
- `checkout_url`;
- webhook;
- firma HMAC-SHA256;
- idempotencia;
- Flyway V12;
- `EventoPagoProveedor`;
- sincronizacion de estado;
- reembolso total;
- reconciliacion individual;
- reconciliacion por lote;
- MVC Cliente;
- MVC BackOffice;
- hardening `live_mode=true`;
- hardening de topics;
- smokes de autorizacion;
- validacion de no exposicion de secretos.

Validaciones de cierre:

- backend tests: OK;
- backend Docker build: OK;
- frontend Release build: OK;
- backend runtime: OK;
- frontend runtime: OK;
- frontend -> backend health: OK;
- checkout anonimo: bloqueado;
- refund anonimo: bloqueado;
- reconciliacion anonima: bloqueada;
- webhook sin firma: HTTP 401;
- provider default: deshabilitado;
- cobros reales durante validacion: NO;
- commit/push de cierre: pendiente de autorizacion explicita.

Siguiente fase del roadmap:

FASE 12 - IA avanzada.
<!-- PHASE11_MERCADOPAGO_CHANGELOG_END -->

<!-- PHASE12_ADVANCED_AI_CHANGELOG_START -->
## 2026-10-22 - Cierre FASE 12 IA avanzada

FASE 12 - IA avanzada: COMPLETADA.

Incluye:

- typed tool layer;
- disponibilidad conversacional;
- recommendation engine;
- server-side reservation drafts;
- RAG con source attribution;
- safe orchestration;
- AI analytics;
- intelligent BackOffice dashboard;
- prompt-injection hardening;
- turn rate limit;
- provider invocation budget;
- MVC conversational UX;
- E2E + failure modes.

Validaciones de cierre:

- backend validator: OK;
- frontend Release build: OK;
- backend runtime: OK;
- frontend runtime: OK;
- anonymous chat: bloqueado;
- RAG `kb://`: validado;
- recommendation: validada;
- availability: validada;
- partial draft: validado;
- ambiguous confirmation creates Reserva: NO;
- prompt injection creates Reserva: NO;
- message > 2000: bloqueado;
- unknown/non-owned conversation: bloqueada;
- E2E real Reservations created: 0;
- payment operations during E2E: 0;
- commit/push de cierre: pendiente de autorizacion explicita.

Documento canonico:

`docs/AI_ADVANCED.md`.
<!-- PHASE12_ADVANCED_AI_CHANGELOG_END -->
