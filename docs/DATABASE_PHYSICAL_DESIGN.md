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

## Referencias

- Diseño lógico: `docs/DATABASE_LOGICAL_DESIGN.md`
- Reglas y restricciones: `docs/DATABASE_RULES.md`

*Documento en construcción según el roadmap definido en `docs/ROADMAP.md`.*
