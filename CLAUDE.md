# Bembos Events

## Objetivo
Sistema académico para gestión y reserva de fiestas/eventos.

## Arquitectura

Navegador
→ ASP.NET Core MVC
→ HTTP/JSON
→ Spring Boot REST API
→ Service
→ Repository
→ JPA/Hibernate
→ Microsoft SQL Server

Reglas:

- El frontend nunca se conecta directamente a SQL Server.
- Spring Boot es la única capa que accede a la base de datos.
- Backend es la fuente de verdad para precios, disponibilidad y reglas.
- No confiar en precios enviados desde frontend.

## Orden de desarrollo

1. Diseño de base de datos.
2. SQL Server.
3. Backend Spring Boot.
4. JPA + Flyway.
5. Reglas de negocio.
6. REST API.
7. Seguridad.
8. Reservas y cotización.
9. Tests backend.
10. Frontend ASP.NET Core MVC.
11. Integración frontend/backend.
12. Mercado Pago Sandbox.
13. Administración.
14. Docker y auditoría.

No avanzar de fase sin autorización del usuario.

## Base de datos

Motor:
Microsoft SQL Server

Nombre:
BembosEventsDB

Entidades previstas:

- Rol
- Usuario
- UsuarioRol
- Cliente
- Sede
- Salon
- HorarioSalon
- Paquete
- Tematica
- ServicioAdicional
- Reserva
- ReservaServicio
- Pago
- HistorialReserva

Dinero:

SQL Server:
DECIMAL

Java:
BigDecimal

Nunca usar double para dinero.

## Backend

Tecnologías:

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven
- Flyway

Seguridad posteriormente:

- Spring Security
- JWT
- BCrypt

Estructura futura:

controller/
service/
service/impl/
repository/
entity/
dto/request/
dto/response/
mapper/
exception/
security/
config/
util/

Nunca exponer entidades JPA directamente desde controllers.

Usar DTOs.

## Roles

Iniciales:

CLIENTE
ENCARGADO_LOCAL
ADMINISTRADOR

Preparar arquitectura para:

ASESOR_EVENTOS
COORDINADOR_EVENTOS
CAJERO
SUPERVISOR
GERENTE

## Reglas de negocio

### Aforo

cantidadInvitados <= capacidad del salón.

### Disponibilidad

No permitir reservas superpuestas para el mismo salón.

Existe superposición cuando:

inicioExistente < finNuevo

Y:

finExistente > inicioNuevo

Ejemplo existente:

15:00 - 18:00

Bloquear:

14:00 - 16:00
16:00 - 17:00
17:00 - 20:00
15:00 - 18:00

Permitir:

12:00 - 15:00
18:00 - 20:00

### Consumo mínimo

Valor inicial:

S/ 500

Debe ser configurable.

### Seña

Valor inicial:

50%

Debe ser configurable.

### Reserva

Debe tener identificador público independiente del ID interno.

Ejemplo:

BEM-2026-000001

## Frontend futuro

Tecnologías:

- C#
- ASP.NET Core MVC
- Razor Views
- ViewModels
- Bootstrap 5
- CSS
- JavaScript
- HttpClient

NO usar Razor Pages como arquitectura principal.

## Mercado Pago

Usar únicamente Sandbox durante desarrollo.

Access Token solamente en backend.

Nunca exponer tokens en:

- frontend
- JavaScript
- Git
- repositorio público

## Forma de trabajo

Antes de modificar código:

1. Leer archivos relacionados.
2. Comprender implementación existente.
3. Realizar cambio mínimo necesario.
4. Ejecutar build/tests.
5. Leer errores reales.
6. Corregir.
7. Volver a ejecutar build/tests.

Ciclo obligatorio:

BUILD/TEST
→ READ
→ EDIT
→ BUILD/TEST

Repetir hasta obtener éxito.

No declarar completado sin verificar.

## Comportamiento del agente

- Ejecutar herramientas reales.
- No simular tool calls escribiendo JSON.
- No preguntar información disponible en el repositorio.
- No inventar propiedades.
- No inventar rutas.
- No cambiar de framework.
- No introducir Python.
- No agregar funcionalidades no solicitadas.
- No avanzar a otra fase automáticamente.