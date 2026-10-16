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
