# Bembos Events — Roadmap

## Orden de desarrollo

El proyecto se desarrollará siguiendo estrictamente este orden:

Base de datos
→ Backend
→ API REST
→ Seguridad
→ Reglas de negocio
→ Tests
→ Frontend
→ Integraciones
→ Administración
→ Docker y auditoría

No avanzar automáticamente entre fases.

---

## FASE 0 — Preparación del proyecto

Objetivo:

Preparar la estructura del repositorio y la configuración de Claude Code.

Incluye:

- estructura base;
- CLAUDE.md;
- README.md;
- agentes;
- Skills;
- reglas por área;
- validación física del repositorio.

Estado esperado:

COMPLETADA antes de iniciar diseño funcional.

---

## FASE 1 — Diseño de base de datos

**Estado:** COMPLETADA ✅

Objetivo:

Diseñar el modelo conceptual y lógico antes de crear SQL.

Definido:

- Modelo conceptual (FASE 1.1) aprobado;
- Modelo lógico con 14 entidades (FASE 1.2A/1.2B) aprobado;
- Reglas y restricciones (FASE 1.3) aprobadas;
- Estados e índices candidatos (FASE 1.4) documentados;
- Auditoría final (FASE 1.5) aprobada;
- Cierre de fase (FASE 1.6) confirmado.

Entidades: Rol, Usuario, UsuarioRol, Cliente, Sede, Salon, HorarioSalon, Paquete, Tematica, ServicioAdicional, Reserva, ReservaServicio, Pago, HistorialReserva.

No crear todavía tablas SQL ni migraciones físicas.

---

## FASE 2 — Microsoft SQL Server

Objetivo:

Implementar físicamente el modelo aprobado.

Incluye:

- creación de base BembosEventsDB;
- tablas;
- PK;
- FK;
- UNIQUE;
- CHECK;
- índices;
- datos iniciales;
- scripts organizados.

---

## FASE 3 — Spring Boot

Objetivo:

Crear la estructura inicial del backend.

Stack:

- Java;
- Spring Boot;
- Maven;
- Spring Web;
- Spring Data JPA;
- Bean Validation;
- SQL Server.

---

## FASE 4 — JPA y Flyway

Objetivo:

Integrar persistencia y migraciones.

Incluye:

- Entities;
- Repositories;
- configuración JPA;
- Flyway;
- mapeo de relaciones.

---

## FASE 5 — Reglas de negocio

Implementar reglas como:

- capacidad de salón;
- disponibilidad;
- horarios;
- superposición de reservas;
- precios;
- consumo mínimo;
- depósito;
- estados de reserva.

---

## FASE 6 — API REST

Crear:

- DTOs;
- Services;
- Controllers;
- validaciones;
- manejo global de errores;
- endpoints REST.

No exponer Entities directamente.

---

## FASE 7 — Seguridad

Implementar:

- usuarios;
- roles;
- BCrypt;
- Spring Security;
- JWT;
- autorización.

Roles iniciales:

- CLIENTE
- ENCARGADO_LOCAL
- ADMINISTRADOR

---

## FASE 8 — Reservas y cotización

Implementar el flujo completo de:

- disponibilidad;
- selección de salón;
- paquete;
- temática;
- servicios adicionales;
- cálculo de precios;
- reserva;
- código público.

---

## FASE 9 — Tests backend

Implementar y ejecutar:

- pruebas unitarias;
- pruebas de servicios;
- pruebas de repositorios;
- pruebas de API;
- reglas de negocio.

---

## FASE 10 — ASP.NET Core MVC

Crear frontend con:

- C#;
- ASP.NET Core MVC;
- Razor Views;
- ViewModels;
- Bootstrap 5;
- CSS;
- JavaScript;
- HttpClient.

El frontend nunca debe conectarse directamente a SQL Server.

---

## FASE 11 — Integración frontend/backend

Integrar ASP.NET Core MVC con la API REST Spring Boot.

Validar:

- errores HTTP;
- autenticación;
- DTOs;
- navegación;
- formularios;
- respuestas de API.

---

## FASE 12 — Mercado Pago Sandbox

Implementar pagos utilizando Mercado Pago Sandbox.

Reglas:

- Access Token únicamente en backend;
- nunca exponer secretos;
- no almacenar datos completos de tarjetas;
- validar pagos desde backend.

---

## FASE 13 — Administración

Implementar funcionalidades administrativas para:

- sedes;
- salones;
- paquetes;
- temáticas;
- servicios;
- reservas;
- pagos;
- usuarios.

---

## FASE 14 — Docker y auditoría final

Objetivo:

Preparar el proyecto para ejecución reproducible y revisión final.

Incluye:

- Docker;
- configuración;
- builds;
- tests;
- revisión de seguridad;
- revisión de secretos;
- documentación final.

---

## Regla de avance

Cada fase debe:

1. implementarse;
2. verificarse físicamente;
3. pasar sus validaciones;
4. cerrarse explícitamente.

No avanzar a la siguiente fase sin autorización del usuario.
