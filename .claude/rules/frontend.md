---
paths:
  - "frontend/**"
---

# Frontend Rules

Aplicar estas reglas únicamente al trabajar dentro de `frontend/`.

Stack futuro:

- C#
- ASP.NET Core MVC
- Razor Views
- ViewModels
- Bootstrap 5
- CSS
- JavaScript
- HttpClient

Reglas:

- El frontend nunca debe conectarse directamente a SQL Server.
- Consumir únicamente la API REST del backend.
- No almacenar secretos o tokens sensibles.
- No calcular precios definitivos.
- No duplicar reglas críticas de negocio.
- Usar ViewModels para las vistas.
- Mantener Controllers MVC ligeros.
- Diseñar interfaz responsive.
- No implementar frontend antes de su fase autorizada.
