# Reglas y Restricciones de Base de Datos — Bembos Events

## FASE 1.3 — REGLAS Y RESTRICCIONES

**Estado:** EN CONSTRUCCIÓN

**SQL generado:** NO  
**Migraciones generadas:** NO

Este documento define reglas lógicas y restricciones del dominio.

No contiene implementación SQL física.

---

## 1. UNICIDAD GLOBAL

- `Rol.nombre` debe ser único — dos roles no pueden tener el mismo nombre.
- `Usuario.correo` debe ser único — una dirección de correo puede asociarse a solo un Usuario.
- `UsuarioRol` no puede repetir la combinación `idUsuario + idRol` — la combinación es única.
- `Cliente` no puede tener múltiples perfiles asociados al mismo `Usuario`.
- `Reserva.codigoReserva` debe ser único e independiente de `idReserva`.

---

## 2. OBLIGATORIEDAD Y OPCIONALIDAD

### Obligatorios conceptualmente:

- `Salon` debe pertenecer a una `Sede`.
- `HorarioSalon` debe pertenecer a un `Salon`.
- `Reserva` debe pertenecer a `Cliente`.
- `Reserva` debe tener `Sede`.
- `Reserva` debe tener `Salon`.
- `ReservaServicio` debe tener `Reserva`.
- `ReservaServicio` debe tener `ServicioAdicional`.
- `Pago` debe pertenecer a `Reserva`.
- `HistorialReserva` debe pertenecer a `Reserva`.

### Opcionales:

- `Cliente.idUsuario` — un cliente puede registrarse sin cuenta asociada.
- `Reserva.idPaquete` — una reserva puede no tener paquete seleccionado.
- `Reserva.idTematica` — una reserva puede no tener temática seleccionada.
- `HistorialReserva.idUsuarioResponsable` — cambios automáticos pueden no tener usuario responsable.

---

## 3. REGLAS MONETARIAS

- Los importes monetarios nunca deben ser negativos.
- `Pago.monto` debe ser mayor que 0.
- `ReservaServicio.cantidad` debe ser mayor que 0.
- `precioUnitario` no debe ser negativo.
- `subtotal` no debe ser negativo.

### Conceptualmente:

`montoBase + montoServicios = montoTotal`

y:

`cantidad * precioUnitario = subtotal`

El backend será la fuente de verdad para cálculos monetarios.

El frontend nunca será fuente de verdad para precios.

---

## 4. CAPACIDAD

- `Salon.capacidad` debe ser mayor que 0.
- `Reserva.cantidadInvitados` debe ser mayor que 0.
- `cantidadInvitados` no puede superar la capacidad del `Salon`.

Conceptualmente:

`cantidadInvitados <= capacidadSalon`

---

## 5. REGLAS TEMPORALES

- `HorarioSalon.horaInicio` debe ser anterior a `horaFin`.
- `Reserva.horaInicio` debe ser anterior a `horaFin`.

### Para detectar superposición:

`inicioExistente < finNuevo`  
AND  
`finExistente > inicioNuevo`

Si ambas condiciones son verdaderas: existe conflicto de horario.

---

## 6. INTEGRIDAD REFERENCIAL

- No deben existir referencias hacia registros inexistentes.
- Las relaciones obligatorias deben conservar integridad.
- Las relaciones opcionales pueden estar ausentes.
- Eliminar o desactivar registros relacionados deberá evaluarse según historial y reglas del negocio.

---

## 7. HISTORIAL Y AUDITORÍA

- `Reserva` mantiene el estado actual.
- `HistorialReserva` conserva cambios relevantes.
- `estadoAnterior` representa el estado previo.
- `estadoNuevo` representa el resultado.
- `fechaCambio` registra cuándo ocurrió.
- `idUsuarioResponsable` identifica al actor cuando corresponda.
- Cambios automáticos pueden no tener usuario responsable.

---

## 8. RESTRICCIONES ESPECÍFICAS POR ENTIDAD

### Rol

- `nombre` es obligatorio.
- `nombre` debe ser único.
- `nombre` no debe estar vacío.
- `activo` representa disponibilidad lógica del rol.
- Desactivar un Rol no debe eliminar su historial de asignaciones.

### Usuario

- `nombres` es obligatorio.
- `apellidos` es obligatorio.
- `correo` es obligatorio.
- `correo` debe ser único.
- `passwordHash` es obligatorio para usuarios con autenticación.
- `passwordHash` nunca almacena contraseña en texto plano.
- `activo` permite deshabilitar el acceso sin eliminar al Usuario.
- La desactivación no debe eliminar información histórica relacionada.

### UsuarioRol

- `idUsuario` es obligatorio.
- `idRol` es obligatorio.
- No puede repetirse la combinación:

`idUsuario + idRol`

- El Usuario referenciado debe existir.
- El Rol referenciado debe existir.

### Cliente

- `idUsuario` puede ser opcional.
- Si existe `idUsuario`, debe referenciar un Usuario válido.
- Un Usuario puede tener como máximo un perfil Cliente.
- `documentoIdentidad`, cuando se registre, no debe estar vacío.
- No duplicar nombres, correo o teléfono provenientes de Usuario sin una justificación funcional.

### Sede

- `nombre` es obligatorio.
- `direccion` es obligatoria.
- Una Sede puede tener múltiples Salones.
- `activo` permite retirar una Sede de nuevas operaciones sin eliminar su historial.
- Una Sede desactivada no debe ofrecerse para nuevas reservas.

### Salon

- `idSede` es obligatorio.
- Cada Salon pertenece exactamente a una Sede.
- `nombre` es obligatorio.
- `capacidad` debe ser mayor que 0.
- Un Salon inactivo no debe ofrecerse para nuevas reservas.
- La desactivación del Salon no debe eliminar reservas históricas.

### HorarioSalon

- `idSalon` es obligatorio.
- `diaSemana` es obligatorio.
- `horaInicio` es obligatoria.
- `horaFin` es obligatoria.
- `horaInicio` debe ser anterior a `horaFin`.
- Cada HorarioSalon pertenece exactamente a un Salon.
- Horarios inactivos no deben considerarse disponibles para nuevas reservas.

No definir todavía tipos SQL.
No crear CHECK constraints físicos.
No crear índices.
No definir ON DELETE u ON UPDATE.

---

## 8. PROHIBIDO

NO escribir:

- CREATE TABLE
- ALTER TABLE
- INT
- BIGINT
- VARCHAR
- NVARCHAR
- DECIMAL
- índices físicos
- ON DELETE
- ON UPDATE
- migraciones Flyway

---

## 9. VERIFICACIÓN

Secciones documentadas:

- Unicidad: OK
- Obligatoriedad: OK
- Reglas monetarias: OK
- Capacidad: OK
- Reglas temporales: OK
- Integridad referencial: OK
- Historial y auditoría: OK
