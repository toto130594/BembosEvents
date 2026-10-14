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
- no implementar todavía Mercado Pago;
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
7. PROHIBIDO
========================================

NO crear:

- CREATE TABLE
- ALTER TABLE
- tipos SQL
- enums Java
- Spring Boot
- índices físicos
- tablas catálogo
- migraciones Flyway

========================================
8. VERIFICACIÓN
========================================

Verifica únicamente que existan:

PENDIENTE
CONFIRMADA
EN_PROCESO
COMPLETADA
CANCELADA
EXPIRADA

y:

APROBADO
RECHAZADO
REEMBOLSADO
