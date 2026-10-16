# Reglas del Proyecto

## Principio principal

Codigo y documentacion forman parte de la misma entrega.

Una funcionalidad con documentacion desactualizada no esta terminada.

## Backend como fuente de verdad

El frontend nunca decide definitivamente:

- disponibilidad.
- capacidad.
- precios.
- permisos.
- transiciones.
- resultado de pagos.

## Capas

```text
Controller
Service
Repository
Database
```

La logica de negocio pertenece a Services.

## Seguridad

- CLIENTE no administra.
- ENCARGADO_LOCAL solo su sede.
- ADMINISTRADOR acceso global.

## Base de datos

Flyway representa la historia del esquema.

Cada cambio nuevo requiere una nueva migracion.

## IA

La IA es un consumidor controlado del backend.

No puede saltarse reglas de negocio.

## Documentacion

| Cambio | Documento |
|---|---|
| Regla de negocio | BUSINESS_RULES.md |
| Endpoint | API.md |
| Seguridad | SECURITY.md |
| Arquitectura | ARCHITECTURE.md |
| Decision tecnica | DECISIONS.md |
| Fase | ROADMAP.md |
| Cambio relevante | CHANGELOG.md |
| Estado general | README.md |

## Definition of Done

- [ ] Codigo terminado.
- [ ] Tests.
- [ ] Build.
- [ ] Migraciones revisadas.
- [ ] Seguridad revisada.
- [ ] API revisada.
- [ ] Postman revisado.
- [ ] README actualizado.
- [ ] Documentacion tecnica actualizada.
- [ ] Git status revisado.
- [ ] Commit descriptivo.
