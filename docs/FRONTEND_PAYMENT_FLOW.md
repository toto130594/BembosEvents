# Frontend Payment Flow

## Estado

Diseno aprobado para FASE 8.0.10.

Este documento define el flujo MVC de pagos del CLIENTE.

La integracion con un proveedor externo de pagos no forma
parte de esta fase.

---

# 1. Arquitectura

Flujo:

Browser
-> ASP.NET Core MVC
-> BackendApiClient
-> Spring Boot REST API
-> Service
-> Repository / JPA
-> SQL Server

Reglas:

- Browser nunca accede directamente a SQL Server.
- Browser nunca administra el JWT de Spring Boot.
- JWT permanece server-side en Session MVC.
- BackendBearerHandler agrega Bearer al HttpClient.
- MVC no decide ownership.
- MVC nunca envia idCliente.
- Spring Boot es autoridad de seguridad y negocio.

---

# 2. Crear pago

Endpoint Spring Boot:

POST /api/reservas/{idReserva}/pagos

Acceso:

CLIENTE propietario de la reserva.

Request exacto:

{
  "monto": 0.00,
  "metodoPago": "..."
}

MVC no envia:

- idCliente
- estadoPago
- referenciaExterna
- fechaPago
- fechaCreacion

Respuesta exitosa:

HTTP 201
PagoResponse

Spring Boot crea el Pago y define su estado.

---

# 3. Listar pagos de una reserva

Endpoint:

GET /api/reservas/{idReserva}/pagos

Acceso:

CLIENTE propietario.

El ownership se valida en Spring Boot.

MVC no consulta SQL para determinar propiedad.

---

# 4. Consultar pago propio

Endpoint:

GET /api/pagos/{idPago}

Acceso:

CLIENTE propietario.

Spring Boot valida que el Pago pertenezca a una Reserva
del CLIENTE autenticado.

Pago ajeno o inexistente no debe revelar informacion.

---

# 5. PagoResponse

Campos oficiales:

- idPago
- idReserva
- monto
- metodoPago
- estadoPago
- referenciaExterna
- fechaPago
- fechaCreacion

MVC solamente representa estos datos.

---

# 6. Estados de Pago

Estados backend:

- PENDIENTE
- APROBADO
- RECHAZADO
- CANCELADO
- REEMBOLSADO

MVC no decide transiciones.

La regla validarTransicionEstado permanece interna en
Spring Boot.

El endpoint tecnico de validacion de transiciones no forma
parte del flujo MVC.

---

# 7. Rutas MVC

Controller propuesto:

PagoController

Rutas:

GET  /reservar/mis-reservas/{idReserva}/pagos

GET  /reservar/mis-reservas/{idReserva}/pagos/nuevo

POST /reservar/mis-reservas/{idReserva}/pagos

GET  /pagos/{idPago}

---

# 8. Listado de pagos

La pagina mostrara contexto de la reserva:

- codigo
- estado
- monto total
- deposito requerido

Luego mostrara los pagos devueltos por Spring Boot.

Por pago:

- idPago
- monto
- metodoPago
- estadoPago
- referenciaExterna
- fechaPago
- fechaCreacion

Empty state:

Aun no existen pagos registrados para esta reserva.

El historial puede consultarse aunque la reserva
posteriormente este CANCELADA.

---

# 9. Nuevo pago

Campos editables:

- monto
- metodoPago

Validaciones UX equivalentes al request backend:

Monto:

- obligatorio
- mayor que cero
- maximo dos decimales

Metodo de pago:

- obligatorio
- maximo 50 caracteres

Spring Boot vuelve a validar todo.

---

# 10. Metodo de pago

El backend actualmente recibe String metodoPago.

No existe un catalogo oficial auditado de metodos.

Por lo tanto MVC no inventara un enum ni lista oficial.

En esta fase se utilizara texto con maximo 50 caracteres.

---

# 11. Monto

El frontend puede mostrar:

- montoTotal
- montoDeposito

obtenidos desde el detalle oficial de Reserva.

MVC no inventara reglas adicionales como:

- monto igual al deposito
- monto igual al total
- monto menor o igual al saldo

salvo que Spring Boot las defina.

---

# 12. POST MVC

Ruta:

POST /reservar/mis-reservas/{idReserva}/pagos

Requisitos:

- Authorize
- ValidateAntiForgeryToken
- PRG
- no idCliente
- no estadoPago
- no referenciaExterna

Request Spring:

- monto
- metodoPago

Si Spring responde 201:

POST
-> Redirect
-> GET /pagos/{idPago}

---

# 13. Detalle Pago

Ruta MVC:

GET /pagos/{idPago}

Consume:

GET /api/pagos/{idPago}

Muestra exclusivamente datos de PagoResponse.

Permite volver al listado de pagos usando idReserva
retornado por Spring Boot.

---

# 14. Seguridad MVC

Todos los endpoints MVC de pagos requieren autenticacion.

Backend 401:

- limpiar JWT backend de Session
- volver a Login
- conservar ReturnUrl local

Backend 403:

- AccessDenied

Backend 404:

- NotFound

Backend 400:

- mostrar error de validacion

Otros errores:

- mensaje generico
- no inventar resultado

---

# 15. Ownership

Ownership pertenece exclusivamente a Spring Boot.

Flujo:

correo autenticado
-> Cliente
-> Reserva propia
-> Pago propio

MVC no compara idCliente.

MVC no accede a tablas.

---

# 16. Integracion con detalle de Reserva

En:

/reservar/mis-reservas/{idReserva}

se agregara:

Ver pagos

El enlace puede mostrarse aun si la Reserva esta CANCELADA,
porque permite consultar historial.

Registrar pago sera una accion independiente.

Spring Boot decidira si el POST puede realizarse.

---

# 17. Dataset DEV

La Reserva:

idReserva = 1
estado = CANCELADA

se conserva como evidencia de FASE 8.0.9.

No sera usada para el primer POST real de Pago.

Para el smoke real se creara una nueva Reserva activa
mediante el flujo normal.

No se insertaran Pagos mediante SQL.

---

# 18. Plan de implementacion

## 8.0.10C

Base tecnica:

- CrearPagoRequestDto
- PagoDto
- BackendApiClient
- PagoController MVC
- ViewModels

Sin POST real.

## 8.0.10D

Consulta:

- listado de pagos
- detalle de pago
- empty state
- enlace desde detalle Reserva
- 401
- 403
- 404

Sin POST real.

## 8.0.10E

Formulario:

- nuevo pago
- validacion MVC
- antiforgery
- POST Spring
- PRG
- errores HTTP

Primero smoke no destructivo.

## 8.0.10F

Smoke real:

- crear Reserva DEV activa
- exactamente un POST real de Pago
- validar HTTP 201
- validar persistencia
- validar listado
- validar detalle
- validar ownership

## 8.0.10G

Cierre:

- backend validator
- frontend Release
- Docker
- auth
- reservas
- pagos
- catalogos
- seguridad
- arquitectura
- git diff

Luego checkpoint commit.

---

# 19. Fuera de alcance

Todavia no:

- Mercado Pago SDK
- Checkout Pro
- Preference
- Webhook
- callback proveedor
- captura de tarjetas
- datos sensibles de tarjetas
- aprobacion manual CLIENTE
- cambio libre de estado
- reembolso real

---

# 20. Autoridad

MVC:

- presenta
- captura input permitido
- llama REST
- interpreta HTTP

Spring Boot:

- autentica
- autoriza
- valida ownership
- valida negocio
- crea Pago
- define estado
- persiste
- responde

SQL Server:

- acceso exclusivo mediante backend/JPA
