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

# 8. ENTIDAD: PAQUETE

## Propósito

Representar los paquetes base disponibles para una reserva de evento.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idPaquete` | Identificador único interno del paquete. |
| `nombre` | Nombre comercial del paquete. |
| `descripcion` | Descripción del contenido del paquete. |
| `precioBase` | Precio base del paquete. |
| `cantidadPersonasBase` | Cantidad de personas incluida como base. |
| `activo` | Indica si el paquete está disponible. |

## Claves

- **PK:** `idPaquete`

## Reglas de Negocio

1. `precioBase` no debe ser negativo.
2. `cantidadPersonasBase` debe ser positiva.
3. Los paquetes inactivos no deben ofrecerse para nuevas reservas.

## Relaciones

- Un Paquete puede utilizarse en múltiples Reservas.
- Una Reserva puede tener 0 o 1 Paquete.

---

# 9. ENTIDAD: TEMATICA

## Propósito

Representar las temáticas o estilos disponibles para personalizar una reserva de evento.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idTematica` | Identificador único interno de la temática. |
| `nombre` | Nombre de la temática. |
| `descripcion` | Descripción de la temática. |
| `precioAdicional` | Importe adicional asociado a la temática. |
| `activo` | Indica si la temática está disponible. |

## Claves

- **PK:** `idTematica`

## Reglas de Negocio

1. `precioAdicional` no debe ser negativo.
2. Las temáticas inactivas no deben ofrecerse para nuevas reservas.

## Relaciones

- Una Tematica puede utilizarse en múltiples Reservas.
- Una Reserva puede tener 0 o 1 Tematica.

---

# 10. ENTIDAD: SERVICIO_ADICIONAL

## Propósito

Representar los servicios opcionales que pueden agregarse a una reserva de evento.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idServicioAdicional` | Identificador único interno del servicio adicional. |
| `nombre` | Nombre del servicio adicional. |
| `descripcion` | Descripción del servicio adicional. |
| `precio` | Precio actual del servicio adicional. |
| `activo` | Indica si el servicio está disponible. |

## Claves

- **PK:** `idServicioAdicional`

## Reglas de Negocio

1. `precio` no debe ser negativo.
2. Los servicios inactivos no deben ofrecerse para nuevas reservas.
3. El precio definitivo aplicado a una reserva se conservará posteriormente en ReservaServicio mediante `precioUnitario`.

## Relaciones

- Un ServicioAdicional puede asociarse a múltiples Reservas mediante ReservaServicio.
- Una Reserva puede contener múltiples ServiciosAdicionales mediante ReservaServicio.
- La relación Reserva ↔ ServicioAdicional es N:M y se resuelve mediante ReservaServicio.

---

# 11. ENTIDAD: RESERVA

## Propósito

Representar la reserva de un evento realizada por un cliente.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idReserva` | Identificador único interno de la reserva. |
| `codigoReserva` | Código público único e independiente del ID interno. |
| `idCliente` | Referencia al cliente que realiza la reserva. |
| `idSede` | Referencia a la sede donde se realizará el evento. |
| `idSalon` | Referencia al salón reservado. |
| `idPaquete` | Referencia opcional al paquete seleccionado. |
| `idTematica` | Referencia opcional a la temática seleccionada. |
| `fechaEvento` | Fecha programada del evento. |
| `horaInicio` | Hora de inicio del evento. |
| `horaFin` | Hora de finalización del evento. |
| `cantidadInvitados` | Cantidad prevista de invitados. |
| `montoBase` | Monto base de la reserva. |
| `montoServicios` | Total correspondiente a servicios adicionales. |
| `montoTotal` | Importe total de la reserva. |
| `porcentajeDeposito` | Porcentaje requerido como depósito. |
| `montoDeposito` | Importe requerido como depósito. |
| `estado` | Estado actual de la reserva. |
| `fechaCreacion` | Fecha de creación de la reserva. |
| `fechaActualizacion` | Fecha de última actualización. |

## Claves

- **PK:** `idReserva`
- **Único:** `codigoReserva`

## Relaciones

- **FK:** `idCliente → Cliente.idCliente`
- **FK:** `idSede → Sede.idSede`
- **FK:** `idSalon → Salon.idSalon`
- **FK opcional:** `idPaquete → Paquete.idPaquete`
- **FK opcional:** `idTematica → Tematica.idTematica`

- Una Reserva pertenece a un Cliente.
- Una Reserva se realiza en una Sede.
- Una Reserva utiliza un Salon.
- Una Reserva puede tener 0 o 1 Paquete.
- Una Reserva puede tener 0 o 1 Tematica.
- Una Reserva puede contener múltiples ServiciosAdicionales mediante ReservaServicio.
- Una Reserva puede tener múltiples Pagos.
- Una Reserva puede tener múltiples registros en HistorialReserva.

## Reglas de Negocio

1. `codigoReserva` debe ser único e independiente de `idReserva`.
2. `cantidadInvitados` debe ser mayor que 0.
3. `cantidadInvitados` no puede superar la capacidad del Salon.
4. `horaInicio` debe ser anterior a `horaFin`.
5. `montoBase` no debe ser negativo.
6. `montoServicios` no debe ser negativo.
7. `montoTotal` no debe ser negativo.
8. `porcentajeDeposito` no debe ser negativo.
9. `montoDeposito` no debe ser negativo.
10. `montoBase + montoServicios = montoTotal`.
11. El backend es la fuente de verdad para precios y cálculos monetarios.

La disponibilidad del salón debe considerar:

`inicioExistente < finNuevo`

AND

`finExistente > inicioNuevo`

El código público puede seguir conceptualmente un formato como:

`BEM-2026-000001`

y no depende del ID interno de la reserva.

---

# 12. ENTIDAD: RESERVA_SERVICIO

## Propósito

Representar la relación entre una Reserva y los ServiciosAdicionales seleccionados.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idReservaServicio` | Identificador único interno del detalle. |
| `idReserva` | Referencia a la reserva. |
| `idServicioAdicional` | Referencia al servicio adicional. |
| `cantidad` | Cantidad contratada del servicio. |
| `precioUnitario` | Precio aplicado al servicio al momento de la reserva. |
| `subtotal` | Importe correspondiente al detalle. |

## Claves

- **PK:** `idReservaServicio`

## Relaciones

- **FK:** `idReserva → Reserva.idReserva`
- **FK:** `idServicioAdicional → ServicioAdicional.idServicioAdicional`

- Una Reserva puede tener múltiples ReservaServicio.
- Un ServicioAdicional puede aparecer en múltiples ReservaServicio.
- ReservaServicio resuelve la relación N:M entre Reserva y ServicioAdicional.

## Reglas de Negocio

1. `cantidad` debe ser mayor que 0.
2. `precioUnitario` no debe ser negativo.
3. `subtotal` no debe ser negativo.
4. `precioUnitario` conserva el precio aplicado al momento de la reserva aunque el precio actual de ServicioAdicional cambie.
5. Conceptualmente:

`cantidad * precioUnitario = subtotal`

---

# 13. ENTIDAD: PAGO

## Propósito

Registrar los pagos asociados a una Reserva.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idPago` | Identificador único interno del pago. |
| `idReserva` | Referencia a la reserva asociada. |
| `monto` | Importe del pago. |
| `metodoPago` | Método utilizado para realizar el pago. |
| `estadoPago` | Estado actual del pago. |
| `referenciaExterna` | Identificador o referencia proveniente de un proveedor externo. |
| `fechaPago` | Fecha efectiva del pago. |
| `fechaCreacion` | Fecha de creación del registro. |

## Claves

- **PK:** `idPago`

## Relaciones

- **FK:** `idReserva → Reserva.idReserva`

- Una Reserva puede tener múltiples Pagos.
- Cada Pago pertenece a una sola Reserva.

## Reglas de Negocio

1. `monto` debe ser mayor que 0.
2. `referenciaExterna` permitirá futuras integraciones con proveedores de pago.
3. Una Reserva puede recibir pagos parciales o múltiples pagos.
4. Los estados de pago serán definidos formalmente en FASE 1.4.

No implementar Mercado Pago todavía.

---

# 14. ENTIDAD: HISTORIAL_RESERVA

## Propósito

Registrar los cambios relevantes en el ciclo de vida de una Reserva para mantener trazabilidad y auditoría.

## Atributos

| Atributo | Descripción |
|----------|-------------|
| `idHistorialReserva` | Identificador único interno del registro de historial. |
| `idReserva` | Referencia a la reserva asociada. |
| `estadoAnterior` | Estado anterior de la reserva. |
| `estadoNuevo` | Nuevo estado de la reserva. |
| `observacion` | Descripción opcional del motivo o detalle del cambio. |
| `fechaCambio` | Fecha y hora en la que ocurrió el cambio. |
| `idUsuarioResponsable` | Usuario responsable del cambio cuando corresponda. |

## Claves

- **PK:** `idHistorialReserva`

## Relaciones

- **FK:** `idReserva → Reserva.idReserva`
- **FK opcional:** `idUsuarioResponsable → Usuario.idUsuario`

- Una Reserva puede tener múltiples registros de HistorialReserva.
- Cada HistorialReserva pertenece a una sola Reserva.
- Un Usuario puede ser responsable de múltiples cambios de historial.
- idUsuarioResponsable puede ser opcional cuando el cambio sea generado automáticamente por el sistema.

## Reglas de Negocio

1. Cada cambio relevante de estado de una Reserva debe poder registrarse en HistorialReserva.
2. `fechaCambio` identifica el momento en que ocurrió la modificación.
3. `estadoAnterior` permite conocer el estado previo.
4. `estadoNuevo` representa el estado resultante.
5. `observacion` puede registrar el motivo o contexto del cambio.
6. HistorialReserva no reemplaza el estado actual almacenado en Reserva.
7. Su propósito es mantener trazabilidad y auditoría.

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
