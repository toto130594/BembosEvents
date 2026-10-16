# Decisiones Arquitectonicas

Este documento registra decisiones importantes del proyecto para evitar perder el contexto tecnico con el tiempo.

## ADR-001 - Java 21

El backend utiliza Java 21.

No se cambia la version sin una decision explicita.

## ADR-002 - Spring Boot

Spring Boot es el backend REST principal.

## ADR-003 - SQL Server

Microsoft SQL Server 2022 es la base de datos principal.

## ADR-004 - Flyway

Flyway es la fuente de verdad para la evolucion del esquema.

Cada cambio estructural nuevo requiere una nueva migracion.

## ADR-005 - ASP.NET Core MVC

El frontend sera desarrollado con ASP.NET Core MVC.

El frontend consumira Spring Boot mediante HTTP y JSON.

## ADR-006 - JWT

La autenticacion actual utiliza JWT stateless.

## ADR-007 - BCrypt

Los passwords se almacenan mediante BCrypt.

Nunca se almacenan passwords en texto plano.

## ADR-008 - Roles

Roles principales:

- CLIENTE.
- ENCARGADO_LOCAL.
- ADMINISTRADOR.

## ADR-009 - UsuarioSede

UsuarioSede representa el alcance territorial de ENCARGADO_LOCAL.

Conceptualmente:

```text
Rol = que puede hacer
UsuarioSede = donde puede hacerlo
```

## ADR-010 - Administrador global

ADMINISTRADOR puede operar sobre todas las sedes.

No necesita UsuarioSede para obtener alcance global.

## ADR-011 - Encargado limitado por sede

ENCARGADO_LOCAL solo puede operar sobre la sede asignada.

Esta restriccion debe aplicarse progresivamente a todos los recursos de BackOffice.

## ADR-012 - Backend como fuente de verdad

El frontend no decide definitivamente:

- disponibilidad.
- capacidad.
- precios.
- permisos.
- pagos.
- transiciones de estado.

## ADR-013 - IA sin acceso directo a base de datos

La IA nunca tendra acceso directo a SQL Server.

Debe utilizar APIs o Services controlados.

## ADR-014 - Chat hibrido

El chat futuro comenzara con IA y podra escalar a un encargado humano.

La conversacion debe conservarse durante el escalamiento.

## ADR-015 - Chat asociado a sede

Cuando la conversacion tenga una sede seleccionada, el escalamiento humano debe dirigirse solamente a encargados autorizados para esa sede.

## ADR-016 - MFA mediante TOTP

El mecanismo previsto para MFA sera TOTP.

Politica objetivo:

- CLIENTE: opcional.
- ENCARGADO_LOCAL: obligatorio.
- ADMINISTRADOR: obligatorio.

Tambien se implementaran codigos de recuperacion.

## ADR-017 - Pagos idempotentes

La integracion futura con Mercado Pago debe soportar idempotencia.

Un mismo evento del proveedor no debe procesarse dos veces.

## ADR-018 - Webhooks seguros

Los webhooks futuros deben validar autenticidad o firma antes de modificar datos.

## ADR-019 - Eventos internos

Se evaluara utilizar eventos internos para desacoplar acciones secundarias.

Ejemplos:

- notificaciones.
- auditoria.
- historial.
- correo.
- chat.

## ADR-020 - Documentacion obligatoria

Codigo y documentacion forman parte de la misma entrega.

Una funcionalidad con documentacion desactualizada se considera incompleta.

## ADR-021 - Endpoints tecnicos temporales

Los endpoints de validacion creados durante las primeras fases sirven para comprobar reglas de negocio.

No todos deben permanecer en la API final.

FASE 7.6 revisara cada endpoint y lo clasificara como:

- definitivo.
- temporal.
- reemplazar.
- eliminar.
- pendiente de implementar.
