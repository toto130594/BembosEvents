# Diseño Físico de Base de Datos — Bembos Events

## FASE 2.1 — MICROSOFT SQL SERVER

**Estado:** EN CONSTRUCCIÓN

**Motor:** Microsoft SQL Server  
**Base planificada:** BembosEventsDB

**SQL ejecutable generado:** NO  
**Migraciones Flyway generadas:** NO

---

## Convenciones físicas

### 14 entidades aprobadas

- Rol
- Usuario
- UsuarioRol
- Cliente
- Sede
- Salon
- HorarioSalon
- Paquete
- Tematica
- ServicioAdicional
- Reserva
- ReservaServicio
- Pago
- HistorialReserva

### Tipos de datos y longitudes iniciales

#### IDs internos
- **INT** — para todos los identificadores internos (idRol, idUsuario, idCliente, etc.)
- No usar BIGINT sin justificación específica

#### Dinero
- **DECIMAL(12,2)** — para todos los valores monetarios
- En Java posteriormente: BigDecimal

#### Porcentajes
- **DECIMAL(5,2)** — para porcentajeDeposito y valores porcentuales similares

#### Valores booleanos
- **BIT** — para atributos activos/inactivos (activo)

#### Fechas
- **DATE** — para fechas sin hora (fechaEvento, fechaRegistro cuando es solo fecha)
- **DATETIME2(0)** — para auditoría de fecha/hora combinada (fechaCreacion, fechaActualizacion, fechaAsignacion, fechaPago, fechaCambio)

#### Horas
- **TIME(0)** — almacena hora, minutos y segundos sin fracciones de segundo
  - Se utiliza para: horaInicio, horaFin

#### Textos Unicode
- **NVARCHAR** — para todos los textos (SQL Server soporta Unicode nativo)
- Longitudes iniciales explícitas:
  - `nombres`: NVARCHAR(100)
  - `apellidos`: NVARCHAR(100)
  - `nombre`: NVARCHAR(100)
  - `correo`: NVARCHAR(254)
  - `telefono`: NVARCHAR(20)
  - `documentoIdentidad`: NVARCHAR(20)
  - `passwordHash`: NVARCHAR(255)
  - `codigoReserva`: NVARCHAR(30)
  - `estado`: NVARCHAR(30)
  - `estadoPago`: NVARCHAR(30)
  - `metodoPago`: NVARCHAR(50)
  - `referenciaExterna`: NVARCHAR(150)
  - `direccion`: NVARCHAR(250)
  - `distrito`: NVARCHAR(100)
  - `descripcion`: NVARCHAR(500)
  - `observacion`: NVARCHAR(500)

#### Restricciones globales
- No crear todavía CREATE TABLE, ALTER TABLE, CREATE INDEX
- No definir todavía constraints físicos (PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK)
- No generar archivos .sql ni migraciones Flyway
- El diseño físico es solo documentación de convenciones

---

## Importantes

### NO crear todavía:
- CREATE DATABASE
- CREATE TABLE
- ALTER TABLE
- CREATE INDEX
- constraints físicos (PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK)
- archivos .sql
- migraciones Flyway

### Esto pertenece a FASE 2.1B y 2.1C:
- Diseño de tipos atributo por atributo para las 14 entidades
- Definición de constraints físicos
- Generación de scripts SQL ejecutables
- Migraciones Flyway

---

## FASE 2.1B y 2.1C — DISEÑO FÍSICO DE LAS 14 ENTIDADES

**Alcance:** diseño físico atributo por atributo de las 14 entidades, tipos SQL Server propuestos, obligatoriedad, PK/FK lógicas, UNIQUE lógicos y observaciones de reglas.

**NO abarca:**
- constraints físicos (PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK) — se implementarán en fase posterior;
- scripts SQL ejecutables (CREATE TABLE, ALTER TABLE) — se implementarán en fase posterior;
- migraciones Flyway — NO pertenecen a FASE 2.

### ENTIDAD 1 — ROL

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idRol | INT | Sí | Sí | No | No | Identificador interno |
| nombre | NVARCHAR(100) | Sí | No | No | Sí | Dos roles no pueden tener el mismo nombre |
| descripcion | NVARCHAR(500) | No | No | No | No | Propuesta física para descripción |
| activo | BIT | Sí | No | No | No | Permite deshabilitar rol sin borrarlo |

---

### ENTIDAD 2 — USUARIO

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idUsuario | INT | Sí | Sí | No | No | Identificador interno |
| nombres | NVARCHAR(100) | Sí | No | No | No | Nombres del usuario |
| apellidos | NVARCHAR(100) | Sí | No | No | No | Apellidos del usuario |
| correo | NVARCHAR(254) | Sí | No | No | Sí | Una dirección de correo puede asociarse a solo un Usuario |
| telefono | NVARCHAR(20) | No | No | No | No | Número de teléfono opcional |
| passwordHash | NVARCHAR(255) | Sí | No | No | No | Hasheo BCrypt, nunca texto plano |
| activo | BIT | Sí | No | No | No | Permite suspender usuario sin borrarlo |
| fechaCreacion | DATETIME2(0) | Sí | No | No | No | Fecha de creación del usuario |
| fechaActualizacion | DATETIME2(0) | Sí | No | No | No | Fecha de última actualización |

---

### ENTIDAD 3 — USUARIO_ROL

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idUsuarioRol | INT | Sí | Sí | No | No | Identificador interno de asignación |
| idUsuario | INT | Sí | No | Sí → Usuario.idUsuario | No | Referencia al usuario |
| idRol | INT | Sí | No | Sí → Rol.idRol | No | Referencia al rol |
| fechaAsignacion | DATETIME2(0) | Sí | No | No | No | Fecha de asignación del rol |

**UNIQUE lógico combinado:** `idUsuario + idRol` — la combinación no debe repetirse. Un usuario no puede tener el mismo rol asignado dos veces.

---

### ENTIDAD 4 — CLIENTE

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idCliente | INT | Sí | Sí | No | No | Identificador interno del cliente |
| idUsuario | INT | No | No | Sí → Usuario.idUsuario (opcional) | Sí cuando existe | Un usuario puede tener como máximo un perfil Cliente. Los datos personales (nombres, apellidos, correo, telefono) permanecen en Usuario y no se duplican aquí. |
| documentoIdentidad | NVARCHAR(20) | No | No | No | No | DNI, CUIL, etc. cuando se registre |
| fechaRegistro | DATE | Sí | No | No | No | Fecha de registro del cliente |
| activo | BIT | Sí | No | No | No | Permite suspender cliente sin borrarlo |

---

### ENTIDAD 5 — SEDE

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idSede | INT | Sí | Sí | No | No | Identificador interno de la sede |
| nombre | NVARCHAR(100) | Sí | No | No | No | Nombre comercial de la sede |
| direccion | NVARCHAR(250) | Sí | No | No | No | Dirección completa de la sede |
| distrito | NVARCHAR(100) | Sí | No | No | No | Distrito o zona donde se encuentra la sede |
| telefono | NVARCHAR(20) | No | No | No | No | Teléfono de contacto de la sede (opcional) |
| activo | BIT | Sí | No | No | No | Permite retirar sede sin borrar historial |

---

### ENTIDAD 6 — SALON

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idSalon | INT | Sí | Sí | No | No | Identificador interno del salón |
| idSede | INT | Sí | No | Sí → Sede.idSede | No | Cada Salón pertenece exactamente a una Sede |
| nombre | NVARCHAR(100) | Sí | No | No | No | Nombre del salón dentro de la sede |
| descripcion | NVARCHAR(500) | No | No | No | No | Características especiales del salón |
| capacidad | INT | Sí | No | No | No | Capacidad máxima de invitados (debe ser > 0) |
| activo | BIT | Sí | No | No | No | Salones inactivos no se ofrecen para nuevas reservas |

---

### ENTIDAD 7 — HORARIO_SALON

| Atributo | Tipo SQL Server | Obligatorio | PK | FK | UNIQUE lógico | Observación |
|----------|-----------------|-------------|-----|-----|---------------|-------------|
| idHorarioSalon | INT | Sí | Sí | No | No | Identificador interno del horario |
| idSalon | INT | Sí | No | Sí → Salon.idSalon | No | Cada HorarioSalon pertenece exactamente a un Salón |
| diaSemana | NVARCHAR(20) | Sí | No | No | No | Día de la semana (Lunes, Martes, etc.) — Propuesta física para representar el día de la semana. |
| horaInicio | TIME(0) | Sí | No | No | No | Hora de inicio (almacena hora, minutos y segundos) |
| horaFin | TIME(0) | Sí | No | No | No | Hora de fin (debe ser > horaInicio) |
| activo | BIT | Sí | No | No | No | Horarios inactivos no se ofrecen para nuevas reservas |

**Regla temporal:** `horaInicio < horaFin` — el inicio debe ser estrictamente anterior a la hora de fin.

---

## Referencias

- Diseño lógico: `docs/DATABASE_LOGICAL_DESIGN.md`
- Reglas y restricciones: `docs/DATABASE_RULES.md`

*Documento en construcción según el roadmap definido en `docs/ROADMAP.md`.*
