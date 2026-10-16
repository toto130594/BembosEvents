# Bembos Events - Reglas de Desarrollo

Estas reglas son obligatorias para cualquier agente que modifique este repositorio.

## Plataforma

- Java 21.
- Spring Boot 3.4.x.
- Windows PowerShell 5.1.
- Microsoft SQL Server.
- Docker.

No cambiar Java 21 sin autorizacion explicita.

## Arquitectura backend

Preferir:

```text
Controller
  ->
Service interface
  ->
ServiceImpl
  ->
Repository
  ->
Database
```

La logica de negocio no debe vivir en Controllers.

## Constructores

Usar constructores explicitos en Controllers, Services y componentes Spring.

No usar @RequiredArgsConstructor mientras exista el problema de diagnostico VS Code/JDT observado en este entorno.

## Lombok y entidades JPA

Permitido:

- @Getter
- @Setter
- @NoArgsConstructor

Evitar:

- @Data en entidades.
- equals/hashCode automaticos sobre relaciones JPA.

## Base de datos

Flyway es la fuente de verdad del esquema.

Todo cambio estructural nuevo requiere una nueva migracion.

No modificar migraciones ya aplicadas para representar cambios nuevos.

## Seguridad

Nunca versionar:

- .env
- passwords.
- JWT.
- JWT_SECRET.
- API keys.
- secretos MFA.

Los permisos siempre se validan en backend.

## Roles

CLIENTE:
- sin BackOffice.

ENCARGADO_LOCAL:
- acceso solo a su sede.

ADMINISTRADOR:
- acceso administrativo global.

## UsuarioSede

Rol responde que puede hacer el usuario.

UsuarioSede responde donde puede hacerlo.

## Reservas

El backend decide definitivamente:

- capacidad.
- disponibilidad.
- superposicion.
- monto minimo.
- precios.
- deposito.
- estados.

## Pagos

La validacion de transiciones debe ocurrir dentro del backend.

El frontend final no debe depender de endpoints tecnicos como validar-transicion.

## IA

La IA nunca puede acceder directamente a SQL Server.

Debe utilizar APIs o Services controlados.

La IA no decide definitivamente:

- precios.
- disponibilidad.
- permisos.
- capacidad.
- aprobacion de pagos.
- transiciones de estado.

## Chat futuro

Debe soportar:

```text
CLIENTE -> IA -> ENCARGADO_LOCAL
```

Al escalar se conserva la misma conversacion.

## MFA futuro

Se utilizara TOTP.

Politica objetivo:

- CLIENTE: opcional.
- ENCARGADO_LOCAL: obligatorio.
- ADMINISTRADOR: obligatorio.

## Tests

- JUnit 5 como base.
- Mockito solo cuando una dependencia requiera aislamiento.
- Toda regla nueva debe tener pruebas apropiadas.

## Documentacion obligatoria

Si cambia negocio -> docs/BUSINESS_RULES.md.

Si cambia API -> docs/API.md y Postman.

Si cambia seguridad -> docs/SECURITY.md.

Si cambia arquitectura -> docs/ARCHITECTURE.md y docs/DECISIONS.md.

Si cambia una fase -> README.md, docs/ROADMAP.md y docs/CHANGELOG.md.

## Definition of Done

Una tarea no esta terminada hasta revisar:

1. Codigo.
2. Tests.
3. Build.
4. Migraciones.
5. Seguridad.
6. API.
7. Postman.
8. README.
9. Documentacion especifica.
10. Git status.
11. Commit.

Documentacion desactualizada significa tarea incompleta.

## PowerShell

Entorno principal: Windows PowerShell 5.1.

No usar &&.

Para bloques complejos usar & { ... }.

Evitar scripts gigantes cuando puedan dividirse en pasos pequenos.

## Git

Antes de commit revisar:

```powershell
git status
git diff
git diff --cached
```

Nunca agregar secretos.
