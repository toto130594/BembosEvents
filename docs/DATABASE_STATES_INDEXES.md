# Estados e Índices — Bembos Events

## FASE 1.4 — ESTADOS E ÍNDICES

Estado: EN CONSTRUCCIÓN

SQL generado: NO
Migraciones generadas: NO

========================================
2. ESTADOS DE RESERVA
========================================

Documenta como propuesta lógica aprobable:

### PENDIENTE

Reserva creada pero todavía no confirmada.

### CONFIRMADA

Reserva validada y confirmada para el evento.

### EN_PROCESO

Evento actualmente en ejecución o atención.

### COMPLETADA

Evento finalizado satisfactoriamente.

### CANCELADA

Reserva cancelada antes de completarse.

### EXPIRADA

Reserva pendiente que perdió validez por no cumplir una condición o plazo requerido.

Aclara:

- Reserva.estado contiene el estado actual.
- HistorialReserva registra las transiciones.
- No implementar todavía enum ni tabla catálogo.

========================================
3. TRANSICIONES DE RESERVA
========================================

Documenta inicialmente:

PENDIENTE → CONFIRMADA
PENDIENTE → CANCELADA
PENDIENTE → EXPIRADA

CONFIRMADA → EN_PROCESO
CONFIRMADA → CANCELADA

EN_PROCESO → COMPLETADA

Aclara que:

- no toda transición es válida;
- las reglas definitivas se implementarán en backend;
- HistorialReserva debe registrar cambios relevantes.

No agregar transiciones inversas sin justificación.

========================================
4. ESTADOS DE PAGO
========================================

Documenta:

### PENDIENTE

Pago creado o iniciado pero todavía sin resultado definitivo.

### APROBADO

Pago confirmado satisfactoriamente.

### RECHAZADO

Pago rechazado por el proveedor o proceso de validación.

### CANCELADO

Pago cancelado antes de completarse.

### REEMBOLSADO

Pago previamente aprobado cuyo importe fue devuelto total o parcialmente según reglas futuras.

Aclara:

- Pago.estadoPago almacena el estado actual del pago.
- Mercado Pago implementado en FASE 11 mediante Orders API;
- no crear enum;
- no crear tabla catálogo.

========================================
5. TRANSICIONES DE PAGO
========================================

Documenta inicialmente:

PENDIENTE → APROBADO
PENDIENTE → RECHAZADO
PENDIENTE → CANCELADO

APROBADO → REEMBOLSADO

Aclara que futuras integraciones de pago podrán requerir estados adicionales.

========================================
6. ESTADO ACTIVO
========================================

Documenta que el atributo:

activo

es diferente de los estados de Reserva y Pago.

Se utiliza para disponibilidad lógica de entidades como:

- Rol
- Usuario
- Cliente
- Sede
- Salon
- HorarioSalon
- Paquete
- Tematica
- ServicioAdicional

activo = false:

- no elimina el registro;
- conserva relaciones históricas;
- evita ofrecer el registro para nuevas operaciones cuando corresponda.

========================================
ÍNDICES CANDIDATOS
========================================

Aclara al inicio:

Los siguientes son índices candidatos a nivel de diseño.
NO están implementados físicamente todavía.
Su definición final se realizará en FASE 2 para Microsoft SQL Server.

========================================
1. ÍNDICES DE UNICIDAD
========================================

Documenta como candidatos:

### Rol

- nombre
- motivo: garantizar búsqueda y unicidad lógica del nombre del rol.

### Usuario

- correo
- motivo: autenticación y búsqueda por correo.
- debe respaldar la unicidad lógica definida previamente.

### UsuarioRol

- combinación idUsuario + idRol
- motivo: evitar asignaciones duplicadas del mismo rol al mismo usuario.

### Cliente

- idUsuario
- motivo: permitir como máximo un perfil Cliente por Usuario cuando idUsuario exista.

### Reserva

- codigoReserva
- motivo: búsquedas públicas y garantizar unicidad del código de reserva.

No crear UNIQUE físico todavía.

========================================
2. ÍNDICES DE RELACIONES
========================================

Documenta como candidatos:

### UsuarioRol

- idUsuario
- idRol

### Cliente

- idUsuario

### Salon

- idSede

### HorarioSalon

- idSalon

### Reserva

- idCliente
- idSede
- idSalon
- idPaquete
- idTematica

### ReservaServicio

- idReserva
- idServicioAdicional

### Pago

- idReserva

### HistorialReserva

- idReserva
- idUsuarioResponsable

Motivo general:

facilitar búsquedas y relaciones por claves foráneas.

No crear índices físicos todavía.

========================================
3. ÍNDICES PARA DISPONIBILIDAD
========================================

Documenta como candidato para Reserva:

- idSalon
- fechaEvento
- horaInicio
- horaFin

Motivo:

la consulta de disponibilidad necesitará localizar rápidamente reservas del mismo salón y fecha antes de evaluar superposición:

inicioExistente < finNuevo
AND
finExistente > inicioNuevo

Aclara que el índice físico exacto será evaluado en SQL Server.

========================================
4. ÍNDICES PARA CONSULTAS FRECUENTES
========================================

Documenta como candidatos:

### Reserva

- estado
- fechaEvento
- idCliente + fechaEvento
- idSede + fechaEvento
- idSalon + fechaEvento

Posibles usos:

- reservas por cliente;
- agenda por salón;
- agenda por sede;
- reservas por fecha;
- reservas por estado.

### Pago

- estadoPago
- fechaPago

Posibles usos:

- pagos pendientes;
- pagos aprobados;
- consultas por fecha.

### HistorialReserva

- idReserva + fechaCambio

Motivo:

consultar cronológicamente el historial de una Reserva.

========================================
5. CAMPOS ACTIVO
========================================

Documenta que podría evaluarse indexación sobre `activo` en entidades con catálogos o disponibilidad:

- Rol
- Usuario
- Cliente
- Sede
- Salon
- HorarioSalon
- Paquete
- Tematica
- ServicioAdicional

Pero aclara:

- NO asumir que un índice solo sobre `activo` será útil;
- la selectividad deberá evaluarse en FASE 2;
- podrían ser más útiles índices compuestos según las consultas reales.

========================================
6. PRINCIPIOS
========================================

Documenta:

- no crear índices innecesarios;
- cada índice tiene costo de almacenamiento y escritura;
- priorizar consultas reales del sistema;
- índices UNIQUE, compuestos y filtrados se decidirán físicamente en FASE 2;
- el diseño lógico no debe fijar todavía sintaxis SQL Server.

========================================
7. PROHIBIDO
========================================

NO crear:

- CREATE TABLE
- ALTER TABLE
- CREATE INDEX
- CREATE UNIQUE INDEX
- INCLUDE
- WHERE filtrado de SQL Server
- tipos SQL
- sintaxis SQL
- enums Java
- Spring Boot
- índices físicos
- tablas catálogo
- migraciones Flyway

========================================
8. VERIFICACIÓN
========================================

Después de editar busca únicamente:

Estados:

PENDIENTE
CONFIRMADA
EN_PROCESO
COMPLETADA
CANCELADA
EXPIRADA
APROBADO
RECHAZADO
CANCELADO
REEMBOLSADO

Y también índices:

ÍNDICES CANDIDATOS
codigoReserva
idSalon + fechaEvento
idReserva + fechaCambio
estadoPago
