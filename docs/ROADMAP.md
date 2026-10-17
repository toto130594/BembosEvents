# Roadmap - Bembos Events

## Objetivo

Construir Bembos Events de manera incremental, manteniendo backend, base de datos, seguridad, frontend y documentacion sincronizados.

# FASE 1 - Modelo de datos

Estado: COMPLETADA.

- entidades.
- relaciones.
- reglas iniciales.

# FASE 2 - SQL Server

Estado: COMPLETADA.

- tablas.
- claves.
- restricciones.
- indices.

# FASE 3 - Spring Boot

Estado: COMPLETADA.

- Java 21.
- Spring Boot.
- Maven.
- Docker.

# FASE 4 - Persistencia

Estado: COMPLETADA.

- JPA.
- entidades.
- repositories.
- Flyway.

# FASE 5 - Reglas de negocio

Estado: COMPLETADA.

- estados de reserva.
- estados de pago.
- capacidad.
- disponibilidad.
- monto minimo.
- deposito.
- transiciones.

# FASE 6 - API tecnica inicial

Estado: COMPLETADA.

- DTO.
- Controllers.
- excepciones.
- endpoints tecnicos de validacion.
- tests.

# FASE 7 - Seguridad

Estado: COMPLETADA.

- BCrypt.
- JWT.
- Spring Security.
- registro.
- login.
- roles.

# FASE 7.5 - Administracion y alcance por sede

Estado: COMPLETADA.

- UsuarioSede.
- CRUD administrativo de usuarios.
- ADMINISTRADOR global.
- ENCARGADO_LOCAL por sede.
- BackOffice de sedes.
- pruebas de autorizacion.
- Postman por roles.

# FASE 7.5F - Sincronizacion documental

Estado: COMPLETADA.

- README.
- reglas para agentes.
- reglas de proyecto.
- arquitectura.
- decisiones.
- negocio.
- seguridad.
- API.
- IA y chat.
- changelog.

# FASE 7.6 - Auditoria REST

Estado: COMPLETADA.

Subfase actual: CERRADA.

Objetivo:

Definir la API REST definitiva antes del frontend.

Avance:

- 7.6B - Contrato REST definitivo: COMPLETADA.
- 7.6C.1 - Catalogos publicos: COMPLETADA.
- 7.6C.2 - Disponibilidad publica: COMPLETADA.
- 7.6D.1 - Consistencia Usuario/Cliente: COMPLETADA.
- 7.6D.2 - Crear reserva real del cliente: COMPLETADA.
- 7.6D.3 - Mis reservas del cliente: COMPLETADA.
- 7.6D.4 - Consultar una reserva propia: COMPLETADA.
- 7.6D.5 - Cancelacion de una reserva propia: COMPLETADA.
- 7.6E - Reservas BackOffice: COMPLETADA.
  - 7.6E.1 - Listar reservas BackOffice: COMPLETADA.
  - 7.6E.2 - Consultar reserva BackOffice: COMPLETADA.
  - 7.6E.3 - Cambiar estado de reserva BackOffice: COMPLETADA.
  - 7.6E.4 - Documentacion, Postman y Git: COMPLETADA.
- 7.6F - Pagos base: COMPLETADA.
  - 7.6F.1 - Pagos Cliente: COMPLETADA.
    - iniciar Pago de una Reserva propia: COMPLETADO.
    - listar Pagos de una Reserva propia: COMPLETADO.
    - consultar Pago propio por id: COMPLETADO.
  - 7.6F.2 - Pagos BackOffice: COMPLETADA.
    - listar Pagos BackOffice: COMPLETADO.
    - consultar Pago BackOffice por id: COMPLETADO.
  - 7.6F.3 - Seguridad y regresion integral: COMPLETADA.
  - 7.6F.4 - Documentacion, Postman y Git: COMPLETADA.

- 7.6G - Retirar endpoints tecnicos: COMPLETADA.
- 7.6H - Documentacion, Postman y tests: COMPLETADA.
- 7.6I - Validacion final y runtime smoke test: COMPLETADA.
Revisar:

- Auth.
- Usuarios.
- Sedes.
- Salones.
- Horarios.
- Paquetes.
- Tematicas.
- Servicios adicionales.
- Reservas.
- Pagos.

Cada endpoint se clasificara como:

- DEFINITIVO.
- TECNICO.
- REEMPLAZAR.
- ELIMINAR.
- PENDIENTE.

Siguiente fase: 7.7 - Seguridad completa por sede.
# FASE 7.7 - Seguridad por sede completa

Estado: PENDIENTE.

Aplicar alcance real de ENCARGADO_LOCAL a:

- reservas.
- pagos.
- operaciones administrativas relacionadas.

# FASE 8 - Frontend ASP.NET Core MVC

Estado: PENDIENTE.

## Portal publico y cliente

- home.
- registro.
- login.
- sedes.
- salones.
- paquetes.
- disponibilidad.
- reservas.
- pagos.

## BackOffice

- dashboard.
- usuarios.
- reservas.
- pagos.
- gestion por sede.

# FASE 9 - Seguridad avanzada

Estado: PENDIENTE.

- recuperacion de password.
- verificacion de correo.
- MFA TOTP.
- recovery codes.
- refresh tokens.
- sesiones.
- rate limiting.
- intentos fallidos.
- auditoria.

# FASE 10 - Chat e IA

Estado: PENDIENTE.

- conversaciones.
- mensajes.
- WebSocket.
- IA.
- escalamiento a ENCARGADO_LOCAL.
- historial.
- notificaciones.

# FASE 11 - Mercado Pago

Estado: PENDIENTE.

- creacion de pago.
- webhook.
- validacion de firma.
- idempotencia.
- reembolsos.
- reconciliacion.

# FASE 12 - IA avanzada

Estado: PENDIENTE.

- disponibilidad conversacional.
- recomendacion de paquetes.
- borradores de reservas.
- RAG.
- analitica.
- dashboard inteligente.

# Regla del roadmap

No se debe marcar una fase como COMPLETADA mientras su codigo y documentacion no esten sincronizados.
