---
paths:
  - "database/**"
---

# Database Rules

Aplicar estas reglas únicamente al trabajar dentro de `database/`.

- Base de datos oficial: Microsoft SQL Server.
- Usar `DECIMAL` para valores monetarios.
- Nunca usar `FLOAT` o `REAL` para dinero.
- Mantener integridad referencial.
- Definir PK y FK explícitamente.
- Usar `UNIQUE`, `CHECK` y `NOT NULL` cuando corresponda.
- No crear índices sin una razón de consulta concreta.
- Las migraciones futuras deben ser compatibles con Flyway.
- No almacenar fechas como texto.
- No usar el ID interno como código público de reserva.
- No crear tablas o columnas no aprobadas por el diseño.
- No realizar operaciones destructivas sin autorización.
