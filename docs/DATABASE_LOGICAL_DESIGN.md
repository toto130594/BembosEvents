# Diseño Lógico de Base de Datos — Bembos Events

## FASE 1.2 — MODELO LÓGICO

**Estado:** EN CONSTRUCCIÓN
**Fase actual:** FASE 1.2 (Modelo Lógico - Parte 1)  
**SQL generado:** NO  
**Migraciones generadas:** NO

---

## Introducción

Este documento amplía el modelo conceptual aprobado en `docs/DATABASE_DESIGN.md` con el **MODELO LÓGICO**. Define de manera formal las entidades, atributos, claves primarias (PK), claves foráneas (FK), restricciones de unicidad y relaciones entre las primeras 7 entidades del sistema: Rol, Usuario, UsuarioRol, Cliente, Sede, Salon y HorarioSalon.

**Importante:** Este documento NO genera tablas SQL, ni migraciones Flyway, ni define tipos de dato específicos (VARCHAR, DECIMAL, INT, etc.). Nos mantenemos en el nivel lógico sin especificaciones físicas.

---

## Restricciones Globales

### Prohibido en esta fase:

- Tipos SQL específicos (INT, BIGINT, VARCHAR, NVARCHAR, DECIMAL, etc.).
- CREATE TABLE o ALTER TABLE statements.
- Definición de índices físicos.
- Migraciones Flyway.
- Datos iniciales concretos.

Este es puramente un documento de diseño lógico conceptual.

---

# 1. ENTIDAD: ROL

## Propósito

Definir los roles funcionales dentro del sistema para gestión de permisos y autorización.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idRol` | Identificador único interno del rol. |
| `nombre` | Nombre del rol (ej: CLIENTE, ADMINISTRADOR). |
| `descripcion` | Descripción detallada del rol y sus permisos. |
| `activo` | Indicador de si el rol está activo o inactivo. |

## Claves

- **PK:** `idRol`
- **Único:** `nombre` (un nombre de rol debe ser único en el sistema)

## Reglas de Negocio

1. El atributo `nombre` es único — dos roles no pueden tener el mismo nombre.
2. El atributo `activo` permite deshabilitar roles sin borrarlos (por historial).
3. Los roles iniciales son: CLIENTE, ENCARGADO_LOCAL, ADMINISTRADOR.

## Relaciones

- **Muchos Roles → Muchos Usuarios** mediante la tabla de unión `UsuarioRol`.
- Un Rol puede estar asociado a múltiples Usuarios.

---

# 2. ENTIDAD: USUARIO

## Propósito

Representar las personas que interactúan con el sistema, incluyendo credenciales, perfil personal y autenticación.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idUsuario` | Identificador único interno del usuario. |
| `nombres` | Nombres del usuario. |
| `apellidos` | Apellidos del usuario. |
| `correo` | Dirección de correo electrónico del usuario. |
| `telefono` | Número de teléfono del usuario. |
| `passwordHash` | Hasheo de la contraseña (nunca texto plano). |
| `activo` | Indicador de si el usuario está activo o inactivo. |
| `fechaCreacion` | Fecha de creación del usuario. |
| `fechaActualizacion` | Fecha de última actualización del usuario. |

## Claves

- **PK:** `idUsuario`
- **Único:** `correo` (una dirección de correo puede asociarse a solo un Usuario)

## Reglas de Negocio

1. El atributo `passwordHash` nunca almacena contraseña en texto plano. Debe usarse hash criptográfico (ej: BCrypt).
2. Un Usuario puede tener uno o más Roles mediante `UsuarioRol`.
3. Los campos `nombres`, `apellidos`, `correo`, `telefono` provienen de la identidad real del usuario, no son datos ficticios.
4. El campo `activo` permite suspender usuarios sin borrarlos (auditoría).

## Relaciones

- **Muchos Usuarios → Muchos Roles** mediante `UsuarioRol`.
- **Un Usuario → 0..1 perfil Cliente**: Un usuario puede tener 0 o 1 perfil Cliente asociado (opcionalmente).
- Los datos de identidad en `Usuario` no deben duplicarse sin justificación en `Cliente`.

---

# 3. ENTIDAD: USUARIO_ROL

## Propósito

Tabla de unión de muchos-a-muchos entre `Usuario` y `Rol`, permitiendo que un usuario tenga uno o más roles simultáneamente.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idUsuarioRol` | Identificador único interno del registro de asignación. |
| `idUsuario` | Referencia al usuario. |
| `idRol` | Referencia al rol. |
| `fechaAsignacion` | Fecha en la que el rol fue asignado al usuario. |

## Claves

- **PK:** `idUsuarioRol`
- **Único (combinada):** `idUsuario + idRol` — la combinación no debe duplicarse

## Relaciones

- **FK:** `idUsuario → Usuario.idUsuario`
- **FK:** `idRol → Rol.idRol`

## Reglas de Negocio

1. La combinación `idUsuario + idRol` es única — un usuario no puede tener el mismo rol asignado dos veces.
2. Un usuario puede tener múltiples roles (CLIENTE y ADMINISTRADOR, por ejemplo).
3. Un rol puede aplicarse a múltiples usuarios.
4. El campo `fechaAsignacion` permite auditar cambios de rol en el tiempo.

---

# 4. ENTIDAD: CLIENTE

## Propósito

Representar a los clientes que realizan reservas de eventos, con información de contacto y su historial de reservas.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idCliente` | Identificador único interno del cliente. |
| `idUsuario` | Referencia opcional al usuario (para autenticación). |
| `documentoIdentidad` | Documento de identidad (DNI, CUIL, etc.). |
| `fechaRegistro` | Fecha de registro del cliente. |
| `activo` | Indicador de si el cliente está activo o inactivo. |

## Claves

- **PK:** `idCliente`
- **FK:** `idUsuario → Usuario.idUsuario` (relación opcional)

## Reglas de Negocio

1. Un Usuario no debe tener múltiples perfiles Cliente — un usuario representa una persona con un solo perfil de cliente.
2. No duplicar nombres, correo o teléfono de `Usuario` sin justificación en `Cliente`.
3. El atributo `idUsuario` es opcional (0..1) — un cliente puede registrarse como invitado sin cuenta asociada.
4. El campo `activo` permite suspender clientes con deuda o comportamiento inapropiado.

## Relaciones

- **Un Cliente → Muchas Reservas** — cada cliente realiza múltiples reservas.
- **Una Reserva → Un solo Cliente** — cada reserva pertenece a un solo cliente.
- **Un Cliente → 0..1 Usuario**: Un cliente puede estar asociado a un solo Usuario (opcional).
- **Un Usuario → 0..1 perfil Cliente**: Un usuario puede tener máximo 1 perfil Cliente asociado.

---

# 5. ENTIDAD: SEDE

## Propósito

Representar las ubicaciones físicas donde se realizan los eventos (salones, locales, etc.).

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idSede` | Identificador único interno de la sede. |
| `nombre` | Nombre comercial de la sede. |
| `direccion` | Dirección completa de la sede. |
| `distrito` | Distrito o zona donde se encuentra la sede. |
| `telefono` | Número de teléfono de contacto de la sede. |
| `activo` | Indicador de si la sede está activa o inactiva. |

## Claves

- **PK:** `idSede`

## Reglas de Negocio

1. Una Sede debe tener nombre y dirección válidos.
2. El campo `activo` permite cerrar sedes sin borrar sus datos históricos.

## Relaciones

- **Una Sede → Muchos Salones** — cada sede puede tener múltiples salones disponibles.
- **Un Salón → Una sola Sede** — cada salón pertenece a una única sede.
- **Una Sede → Muchas Reservas** — múltiples reservas pueden realizarse en una misma sede.

---

# 6. ENTIDAD: SALON

## Propósito

Representar los espacios físicos disponibles dentro de cada sede para eventos (capacidad, características).

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idSalon` | Identificador único interno del salón. |
| `idSede` | Referencia a la sede que contiene el salón. |
| `nombre` | Nombre del salón dentro de la sede. |
| `descripcion` | Descripción detallada del salón (características especiales). |
| `capacidad` | Capacidad máxima de invitados para el salón. |
| `activo` | Indicador de si el salón está disponible o no. |

## Claves

- **PK:** `idSalon`
- **FK:** `idSede → Sede.idSede`

## Reglas de Negocio

1. El atributo `capacidad` debe ser positiva (valor > 0).
2. Un Salón pertenece a una sola Sede (no puede migrar entre sedes).
3. Los salones inactivos no deben aparecer en búsquedas de disponibilidad pública.
4. La relación con `Sede` es obligatoria (N:1) — un salón debe existir dentro de exactamente una sede.

## Relaciones

- **Un Salón → Muchos HorarioSalon**: Un salón tiene múltiples ventanas de disponibilidad (1:N).
- **Un Salón → Muchas Reservas**: Un salón puede ser reservado en múltiples momentos no simultáneos (1:N).

---

# 7. ENTIDAD: HORARIO_SALON

## Propósito

Definir los horarios disponibles para cada salón en días específicos de la semana, gestionando la disponibilidad temporal.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idHorarioSalon` | Identificador único interno del horario. |
| `idSalon` | Referencia al salón para el cual es válido el horario. |
| `diaSemana` | Día de la semana (ej: Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo). |
| `horaInicio` | Hora de inicio del horario disponible. |
| `horaFin` | Hora de fin del horario disponible. |
| `activo` | Indicador de si el horario está activo o no. |

## Claves

- **PK:** `idHorarioSalon`
- **FK:** `idSalon → Salon.idSalon`

## Reglas de Negocio

1. El atributo `horaInicio` debe ser estrictamente anterior a `horaFin`.
2. Un Salón puede tener múltiples HorariosSalon (diferentes días o turnos).
3. Los horarios inactivos no deben bloquear disponibilidad pública.
4. La verificación de superposición se realiza combinando:
   - `inicioExistente < finNuevo` Y `finExistente > inicioNuevo`

## Relaciones

- **Un HorarioSalon → Un solo Salón** — cada horario pertenece a un salón específico.
- **Un Salón → Muchos HorarioSalon** — los salones pueden tener múltiples ventanas de disponibilidad.

---

# Resumen de Relaciones (Primeras 7 Entidades)

| Relación | Entidad 1 | Entidad 2 | Cardinalidad Lógica | Descripción |
|----------|-----------|-----------|---------------------|-------------|
| Usuario → Rol (vía UsuarioRol) | Usuario | Rol | N:M | Un usuario puede tener múltiples roles; un rol puede aplicarse a múltiples usuarios. |
| Cliente → Usuario | Cliente | Usuario | 1:0..1 | Un cliente puede tener un usuario opcional para autenticación. |
| Sede → Salon | Sede | Salon | 1:N | Una sede tiene muchos salones; cada salón pertenece a una sede. |
| Salon → HorarioSalon | Salon | HorarioSalon | 1:N | Un salón tiene múltiples horarios disponibles. |
| Cliente → Reserva | Cliente | Reserva | 1:N | Un cliente puede realizar muchas reservas; una reserva pertenece a un solo cliente. |
| Sede → Reserva | Sede | Reserva | 1:N | Una sede recibe múltiples reservas. |
| Salon → Reserva | Salon | Reserva | 1:N | Un salón puede ser reservado en múltiples momentos no simultáneos. |
| UsuarioRol → Usuario | UsuarioRol | Usuario | N:1 | Múltiples asignaciones de rol para un usuario (FK en UsuarioRol). |
| UsuarioRol → Rol | UsuarioRol | Rol | N:1 | Múltiples asignaciones de rol a un mismo rol (FK en UsuarioRol). |

---

# Límites de esta Fase (FASE 1.2A)

**NO se definirá:**

- Tipos SQL específicos (VARCHAR, DECIMAL, INT, BIGINT, etc.).
- Nombres de columnas definitivos para las entidades restantes.
- CREATE TABLE statements.
- Índices físicos.
- Scripts SQL organizados.
- Migraciones Flyway.
- Datos iniciales concretos.
- Implementación backend o frontend.

**Entidades documentadas en esta fase:** 7 de 14 (Rol, Usuario, UsuarioRol, Cliente, Sede, Salon, HorarioSalon).

**Entidades pendientes para FASE 1.2B:** Paquete, Tematica, ServicioAdicional, Reserva, ReservaServicio, Pago, HistorialReserva.

---

## Siguientes Pasos

Una vez aprobado este diseño lógico parcial:

1. **FASE 1.2B:** Documentar las 7 entidades restantes (Paquete, Tematica, ServicioAdicional, Reserva, ReservaServicio, Pago, HistorialReserva).
2. **FASE 1.3:** Reglas y restricciones del modelo lógico completo.
3. **FASE 1.4:** Índices lógicos y estados de validez.
4. **FASE 1.5:** Revisión con database-check.
5. **FASE 1.6:** Cierre de FASE 1.
6. **FASE 2:** Implementación en Microsoft SQL Server.

*Documento en construcción según el roadmap definido en `docs/ROADMAP.md`.*
