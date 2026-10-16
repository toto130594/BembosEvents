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