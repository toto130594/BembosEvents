---
paths:
  - "backend/**"
---

# Backend Rules

Aplicar estas reglas únicamente al trabajar dentro de `backend/`.

## Java

- Java 21.
- Spring Boot 3.4.1.
- Usar características modernas de Java cuando mejoren claridad y mantenibilidad.
- Lambdas, Streams y Optional se usan cuando aporten valor real.
- NO reemplazar if, switch o for únicamente por usar sintaxis moderna.
- Prioridad: claridad > mantenibilidad > modernidad.

## Lombok

- Lombok está permitido en el backend.
- En entidades JPA preferir:
  - `@Getter`
  - `@Setter`
  - `@NoArgsConstructor`
- NO usar `@Data` en entidades JPA.
- NO generar automáticamente equals/hashCode/toString sobre relaciones JPA.
- Evitar anotaciones Lombok que puedan provocar recursión con relaciones bidireccionales.
- Lombok debe reducir boilerplate, no ocultar lógica de negocio.
- Enums, repositories y clases de dominio puras no necesitan Lombok salvo que aporte valor real.

## JPA

- Mantener `FetchType.LAZY` en relaciones donde ya está definido.
- NO agregar cascade u orphanRemoval sin una decisión explícita.
- NO exponer Entities directamente por REST.
- DTOs se usarán en la capa API.
- Dinero siempre con `BigDecimal`, nunca float ni double.
- `spring.jpa.hibernate.ddl-auto=none`.
- Flyway es la fuente canónica de migraciones.

## Testing

- JUnit 5 es el framework base de pruebas.
- Para lógica pura de dominio usar JUnit 5 sin Mockito.
- Usar Mockito cuando una clase tenga dependencias que deban aislarse, especialmente Services que dependan de Repositories.
- Para Mockito usar cuando corresponda:
  - `@ExtendWith(MockitoExtension.class)`
  - `@Mock`
  - `@InjectMocks`
  - `when(...)`
  - `verify(...)`
  - `ArgumentCaptor` si aporta valor.
- NO mockear clases sin dependencias solo por usar Mockito.
- Pruebas de Repository con base de datos/JPA son pruebas de integración, no mocks.
- Para Controllers REST usar JUnit 5 + MockMvc y mockear la capa Service cuando corresponda.
- Los tests deben validar casos válidos, inválidos, límites y null cuando aplique.

## Arquitectura

- Mantener:
  - Controller -> Service -> Repository -> Entity
- Reglas de negocio deben vivir en dominio/servicio, no en Controller.
- Backend es la autoridad final de validaciones y precios.
- Frontend nunca accede directamente a SQL Server.

## Seguridad de Edición

- Archivo nuevo -> Write.
- Archivo existente importante -> Update/Edit.
- Si Update/Edit falla -> DETENERSE.
- NO usar Write como fallback sobre archivos existentes importantes.
- NO modificar archivos fuera del alcance solicitado.
- NO ejecutar Docker, Git ni otros comandos salvo que la tarea lo pida expresamente.

## Reglas de Negocio (Resumen)

- Aforo: cantidadInvitados <= capacidad del salón.
- Disponibilidad: No permitir reservas superpuestas para el mismo salón.
- Consumo mínimo: Valor inicial S/ 500, configurable.
- Seña: Valor inicial 50%, configurable.
- Reserva: Identificador público independiente (ej: BEM-2026-000001).
