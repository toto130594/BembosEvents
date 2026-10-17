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
