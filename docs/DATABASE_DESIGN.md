# Diseño de Base de Datos — Bembos Events

## FASE 1.1 — MODELO CONCEPTUAL

**Estado:** Documento conceptual aprobado  
**Fase actual:** FASE 1.1 (Modelo Conceptual)  
**SQL generado:** NO  
**Migraciones generadas:** NO

---

## Introducción

Este documento define el **MODELO CONCEPTUAL** de la base de datos para Bembos Events. El propósito de esta fase es establecer las entidades, sus responsabilidades, relaciones y cardinalidades sin definir tipos SQL específicos ni scripts de implementación.

**Importante:** Esta fase NO genera tablas SQL, ni migraciones Flyway, ni define tipos de dato definitivos (VARCHAR, DECIMAL, INT, etc.). Todo el enfoque está en las relaciones conceptuales del dominio de negocio.

---

## Entidades Principales

### 1. Rol
- **Propósito:** Definir los roles funcionales dentro del sistema.
- **Responsabilidad:** Clasificar permisos y capacidades de acceso (CLIENTE, ENCARGADO_LOCAL, ADMINISTRADOR, etc.).
- **Relaciones principales:** Relacionado con Usuario a través de la tabla de unión UsuarioRol.
- **Cardinalidades conceptuales:** Un Rol puede estar asociado a múltiples Usuarios.

### 2. Usuario
- **Propósito:** Representar las personas que interactúan con el sistema.
- **Responsabilidad:** Gestionar credenciales, perfil personal y autenticación de cada usuario registrado.
- **Relaciones principales:** Asociado a uno o más Roles (a través de UsuarioRol); relación con Cliente.
- **Cardinalidades conceptuales:** Un Usuario puede tener múltiples Roles, y un Cliente puede asociarse a un Usuario.

### 3. UsuarioRol
- **Propósito:** Tabla de unión de muchos-a-muchos entre Usuario y Rol.
- **Responsabilidad:** Permitir que un usuario tenga uno o más roles simultáneamente.
- **Relaciones principales:** Une Usuario con Rol.
- **Cardinalidades conceptuales:** Muchos a Muchos (Usuario ↔ Rol).

### 4. Cliente
- **Propósito:** Representar a los clientes que realizan reservas de eventos.
- **Responsabilidad:** Gestionar información del cliente, datos de contacto y su historial de reservas.
- **Relaciones principales:** Asociado a Usuario (opcional, para autenticación); realiza múltiples Reservas.
- **Cardinalidades conceptuales:** Un Cliente realiza muchas Reservas; una Reserva pertenece a un solo Cliente.

### 5. Sede
- **Propósito:** Representar las ubicaciones físicas donde se realizan los eventos.
- **Responsabilidad:** Gestionar información sobre cada sede.
- **Relaciones principales:** Contiene múltiples Salones; se relaciona con Reserva.
- **Cardinalidades conceptuales:** Una Sede tiene muchos Salones; un Salón pertenece a una sola Sede.

### 6. Salon
- **Propósito:** Representar los espacios físicos disponibles dentro de cada sede para eventos.
- **Responsabilidad:** Gestionar capacidad, características y disponibilidad del espacio.
- **Relaciones principales:** Pertenece a una Sede; tiene HorariosSalon; participa en Reservas.
- **Cardinalidades conceptuales:** Un Salón está asociado a una sola Sede; puede tener múltiples HorariosSalon.

### 7. HorarioSalon
- **Propósito:** Definir los horarios disponibles para cada salón en días específicos.
- **Responsabilidad:** Gestionar la disponibilidad temporal de los salones.
- **Relaciones principales:** Pertenece a un Salón; relacionado con reglas de disponibilidad y superposición.
- **Cardinalidades conceptuales:** Un Salón tiene múltiples HorariosSalon; cada HorarioSalon pertenece a un Salón.

### 8. Paquete
- **Propósito:** Definir los paquetes base que incluyen el evento.
- **Responsabilidad:** Gestionar precios y configuraciones de los paquetes disponibles para reserva.
- **Relaciones principales:** Asociado a una Reserva; forma parte del cálculo de costos.
- **Cardinalidades conceptuales:** Una Reserva puede asociarse a un Paquete; un Paquete puede ser usado en múltiples Reservas.

### 9. Tematica
- **Propósito:** Definir las temáticas o estilos disponibles para el evento.
- **Responsabilidad:** Gestionar opciones de decoración y ambientación según la temática seleccionada.
- **Relaciones principales:** Asociado a una Reserva; forma parte de la configuración del evento.
- **Cardinalidades conceptuales:** Una Reserva puede tener una Tematica; múltiples Reservas pueden usar la misma Tematica.

### 10. ServicioAdicional
- **Propósito:** Definir servicios opcionales que pueden agregarse a una reserva.
- **Responsabilidad:** Gestionar los servicios extra disponibles con su respectivo costo.
- **Relaciones principales:** Asociado a ReservaServicio; forma parte del cálculo de precios totales.
- **Cardinalidades conceptuales:** Un ServicioAdicional puede asociarse a múltiples Reservas a través de ReservaServicio.

### 11. Reserva
- **Propósito:** Representar una reserva de evento concretada por un cliente.
- **Responsabilidad:** Gestionar toda la información sobre el evento reservado.
- **Relaciones principales:** Pertenece a Cliente; se realiza en Sede; utiliza Salon; puede asociarse a Paquete y Tematica; puede incluir múltiples ServiciosAdicionales; tiene Pagos e HistorialReserva.
- **Cardinalidades conceptuales:** Un Cliente tiene muchas Reservas; una Sede recibe muchas Reservas; un Salón puede tener múltiples Reservas no simultáneas.

### 12. ReservaServicio
- **Propósito:** Tabla de unión entre Reserva y ServicioAdicional.
- **Responsabilidad:** Permitir que una reserva incluya múltiples servicios adicionales, con cantidad y ajustes de precio.
- **Relaciones principales:** Une Reserva con ServicioAdicional.
- **Cardinalidades conceptuales:** Muchos a Muchos (Reserva ↔ ServicioAdicional).

### 13. Pago
- **Propósito:** Registrar los pagos realizados por el cliente para la reserva.
- **Responsabilidad:** Gestionar historial de transacciones de pago, monto, fecha, método y estado.
- **Relaciones principales:** Pertenece a una Reserva; puede representar cumplimiento de consumo mínimo o depósito.
- **Cardinalidades conceptuales:** Una Reserva tiene uno o más Pagos; un Pago pertenece a una sola Reserva.

### 14. HistorialReserva
- **Propósito:** Guardar registros de cambios de estado o movimientos importantes en una reserva.
- **Responsabilidad:** Auditoría y trazabilidad del ciclo de vida de la reserva.
- **Relaciones principales:** Pertenece a una Reserva.
- **Cardinalidades conceptuales:** Una Reserva puede tener múltiples registros en HistorialReserva; un registro pertenece a una sola Reserva.

---

### Nota adicional — Código público y reglas de negocio

Aunque no es una entidad física, el modelo conceptual debe considerar:

- **Código público de reserva:** Identificador independiente del ID interno (ej.: BEM-2026-000001), asociado a la Reserva para exposición pública.
- **Capacidad del salón:** Una Reserva debe respetar `cantidadInvitados <= capacidad`.
- **Disponibilidad:** No permitir reservas superpuestas para el mismo salón.
- **Estados de reserva:** Estados conceptuales como PENDING, CONFIRMED, ACTIVE, CANCELLED, COMPLETED y EXPIRED.
- **Consumo mínimo y depósito:** Reglas monetarias configurables que afectan la validación de reservas.

---

## Resumen de Relaciones Clave

| Relación | Entidad 1 | Entidad 2 | Cardinalidad | Descripción |
|---|---|---|---|---|
| Usuario → Rol (vía UsuarioRol) | Usuario | Rol | N:M | Un usuario puede tener múltiples roles; un rol puede aplicarse a múltiples usuarios. |
| Cliente → Usuario | Cliente | Usuario | 1:0..1 | Un cliente puede tener un usuario opcional para autenticación. |
| Sede → Salon | Sede | Salon | 1:N | Una sede tiene muchos salones; cada salón pertenece a una sede. |
| Salon → HorarioSalon | Salon | HorarioSalon | 1:N | Un salón tiene múltiples horarios disponibles. |
| Cliente → Reserva | Cliente | Reserva | 1:N | Un cliente puede realizar muchas reservas. |
| Sede → Reserva | Sede | Reserva | 1:N | Una sede recibe múltiples reservas. |
| Salon → Reserva | Salon | Reserva | 1:N | Un salón puede ser reservado en múltiples momentos no superpuestos. |
| Paquete → Reserva | Paquete | Reserva | 1:N | Una reserva puede asociarse a un paquete opcional. |
| Tematica → Reserva | Tematica | Reserva | 1:N | Una reserva puede tener una temática opcional. |
| ServicioAdicional ↔ Reserva | ServicioAdicional | Reserva | N:M | Se resuelve mediante ReservaServicio. |
| Pago → Reserva | Pago | Reserva | N:1 | Múltiples pagos para una reserva. |
| HistorialReserva → Reserva | HistorialReserva | Reserva | N:1 | Múltiples registros de historial para una reserva. |

---

## Notas Conceptuales Importantes

### Integridad Referencial
Todas las relaciones deben mantener integridad referencial apropiada, excepto donde se indique opcionalidad.

### No-Atomicidad de Pagos
Una reserva puede tener múltiples pagos (seña inicial, saldo pendiente, etc.). Los pagos son independientes temporalmente pero lógicamente pertenecen a una misma Reserva.

### Separación Historia-Estado Actual
La Reserva guarda el estado actual y la composición del evento. HistorialReserva captura los eventos temporales. Esta separación permite auditoría sin alterar datos principales.

### Identificador Público vs ID Interno
- **ID interno:** Clave primaria para uso en base de datos.
- **Código público:** Identificador legible como `BEM-2026-000001` para comunicación externa.

### Modelo Temporal de Disponibilidad
HorarioSalon define ventanas temporales disponibles. El sistema debe verificar superposición temporal antes de permitir reservas. Esta lógica se implementará en FASE 5, pero es conceptualmente parte del modelo.

---

## Límites de esta Fase (FASE 1.1)

**NO se definirá:**

- Tipos SQL específicos (VARCHAR, DECIMAL, INT, BIGINT, etc.).
- Nombres de columnas definitivos.
- CREATE TABLE statements.
- Índices físicos.
- Scripts SQL organizados.
- Migraciones Flyway.
- Datos iniciales concretos.
- Implementación backend o frontend.

**Todo lo anterior se realizará en FASE 2 (Microsoft SQL Server), tras el diseño conceptual aprobado.**

---

## Siguientes Pasos

Una vez aprobado este modelo conceptual:

1. **FASE 1:** Revisión y diseño lógico.
2. **FASE 2 — Microsoft SQL Server:** Conversión a implementación física.
3. **Resto del proyecto** siguiendo `docs/ROADMAP.md`.

---

*Documento reconstruido a partir del registro de la versión aprobada de FASE 1.1.*  
*No contiene código SQL ni migraciones.*
