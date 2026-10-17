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
