---
paths:
  - "backend/**"
---

# Backend Rules

Aplicar estas reglas únicamente al trabajar dentro de `backend/`.

Stack:

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven
- Flyway

Reglas:

- Controller: HTTP y delegación.
- Service: reglas de negocio.
- Repository: persistencia.
- Usar DTOs para entrada y salida.
- No exponer Entities directamente desde REST.
- Usar `BigDecimal` para dinero.
- No usar `double` o `float` para importes.
- El backend es la fuente de verdad para precios y reglas.
- No confiar en valores calculados por frontend.
- Después de cambios relevantes ejecutar build/tests.
- No introducir frameworks no aprobados.
- No avanzar a funcionalidades de fases futuras.
