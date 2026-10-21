# Mercado Pago - Bembos Events

Estado: **FASE 11 COMPLETADA**.

Fecha de cierre tecnico/documental: 2026-10-21.

## 1. Alcance

La integracion utiliza Mercado Pago Checkout Pro sobre Orders API.

La Fase 11 implementa:

- configuracion TEST server-side;
- creacion de Order;
- `checkout_url`;
- webhook;
- validacion HMAC-SHA256;
- idempotencia;
- persistencia de eventos del proveedor;
- sincronizacion de estado;
- reembolso total;
- reconciliacion manual;
- MVC Cliente;
- MVC BackOffice;
- smoke tests y hardening.

La Fase 11 **no habilita produccion**.

`MERCADOPAGO_ENABLED` permanece `false` por defecto.

Durante las validaciones de la fase no se realizo ningun cobro ni reembolso real.

## 2. Variables de configuracion

```text
MERCADOPAGO_ENABLED
MERCADOPAGO_API_BASE_URL
MERCADOPAGO_ACCESS_TOKEN_TEST
MERCADOPAGO_WEBHOOK_SECRET_TEST
```

Reglas:

- Access Token: solo backend.
- Webhook Secret: solo backend.
- no imprimir secretos en logs;
- no enviar secretos al navegador;
- no persistir secretos en SQL Server.

## 3. Arquitectura

```text
Browser
   |
   v
ASP.NET Core MVC
   |
   | cookie MVC
   | JWT backend guardado solamente en Session server-side
   v
Spring Boot REST
   |
   +--> PagoService
   |
   +--> MercadoPagoProvider
   |        |
   |        v
   |   Mercado Pago Orders API
   |
   +--> SQL Server
```

Spring Boot es la autoridad final.

El navegador nunca recibe:

- backend JWT;
- backend refresh token;
- Mercado Pago Access Token;
- Mercado Pago Webhook Secret.

## 4. Checkout Cliente

Endpoint backend:

```http
POST /api/reservas/{idReserva}/pagos/checkout
```

Acceso:

- `CLIENTE`;
- la Reserva debe pertenecer al Cliente autenticado.

El navegador envia el monto del intento de pago.

El backend fuerza:

```text
metodoPago = MERCADO_PAGO
```

Flujo:

```text
CLIENTE
   |
   v
Spring crea Pago PENDIENTE
   |
   v
POST /v1/orders
   |
   +-- Authorization: Bearer <server-side>
   +-- X-Idempotency-Key
   +-- external_reference = PAGO-{idPago}
   |
   v
Mercado Pago Order
   |
   +-- order id
   +-- status
   +-- status_detail
   +-- checkout_url
   |
   v
order id -> Pago.referenciaExterna
   |
   v
MVC valida checkout_url HTTPS permitida
   |
   v
redirect del comprador
```

La respuesta del navegador nunca aprueba el Pago.

## 5. Orders API

Operaciones utilizadas:

```http
POST /v1/orders
GET  /v1/orders/{orderId}
POST /v1/orders/{orderId}/refund
```

Las operaciones de escritura utilizan `X-Idempotency-Key`.

## 6. Webhook

Endpoint:

```http
POST /api/webhooks/pagos/mercado-pago
```

Es el unico endpoint de pagos anonimo de la integracion.

La autenticidad se valida con:

```text
x-signature
x-request-id
data.id
```

Formato de firma:

```text
ts=<timestamp>,v1=<hmac>
```

Manifest utilizado:

```text
id:<data.id lowercase>;request-id:<x-request-id>;ts:<ts>;
```

Algoritmo:

```text
HMAC-SHA256
```

La comparacion se realiza en tiempo constante.

Firma ausente o invalida:

```text
HTTP 401
```

La firma completa:

- no se persiste;
- no se imprime en logs.

## 7. Hardening de desarrollo

Este proyecto usa Mercado Pago solo en desarrollo/test.

Una notificacion correctamente firmada con:

```json
{
  "live_mode": true
}
```

se reconoce con HTTP 200 y resultado:

```text
LIVE_MODE_IGNORED
```

sin:

- persistir evento;
- consultar estado;
- modificar Pago.

Un topic firmado distinto de `order` se reconoce con:

```text
UNSUPPORTED_TOPIC_IGNORED
```

tambien sin efectos de negocio.

## 8. Idempotencia de eventos

Flyway:

```text
V12__mercadopago_provider_events.sql
```

Tabla:

```text
EventoPagoProveedor
```

Identidad unica:

```text
UNIQUE(proveedor, idEventoExterno)
```

Preferencia para `idEventoExterno`:

1. notification `id`;
2. fingerprint SHA-256 estable de:
   - topic;
   - action;
   - resource id;
   - date_created.

Un evento con `procesado=true`:

```text
DUPLICATE_IGNORED
```

Un evento existente con `procesado=false` puede reintentarse.

Esto evita perder un webhook cuando la consulta al proveedor falla temporalmente.

## 9. Sincronizacion de estado

El webhook no confia en el body para aprobar el Pago.

Despues de firma e idempotencia:

```text
GET /v1/orders/{orderId}
```

Mercado Pago es la fuente externa de verdad.

Mapeo implementado:

| Mercado Pago | Estado interno |
|---|---|
| `processed/accredited` | `APROBADO` |
| `failed/*` | `RECHAZADO` |
| `canceled/*` | `CANCELADO` |
| `refunded` o `processed/refunded` | `REEMBOLSADO` |
| `created/*` | sin cambio |
| `processing/*` | sin cambio |
| `action_required/*` | sin cambio |
| `processed/partially_refunded` | sin cambio |

Toda mutacion pasa por las reglas de transicion de `PagoService`.

No se ejecuta HTTP externo dentro de una transaccion JPA larga.

## 10. Reembolsos

Fase 11 implementa **solo reembolso total**.

Endpoint BackOffice:

```http
POST /api/backoffice/pagos/{idPago}/reembolso
```

Acceso:

```text
ADMINISTRADOR
```

Requisitos:

- Pago `MERCADO_PAGO`;
- `referenciaExterna` presente;
- estado local `APROBADO`;
- Order del proveedor acreditada.

Proveedor:

```http
POST /v1/orders/{orderId}/refund
```

El body del reembolso total es vacio.

Se envia `X-Idempotency-Key` deterministico por Pago/Order.

El estado local cambia a `REEMBOLSADO` solamente despues de confirmacion del proveedor.

Reembolsos parciales permanecen fuera del alcance de Fase 11 porque el dominio actual no modela saldo reembolsado parcial.

## 11. Reconciliacion

Individual:

```http
POST /api/backoffice/pagos/mercado-pago/{idPago}/reconciliar
```

Lote:

```http
POST /api/backoffice/pagos/mercado-pago/reconciliar
```

Acceso:

```text
ADMINISTRADOR
```

El lote procesa como maximo los 100 Pagos Mercado Pago mas recientes con Order id.

Resultados posibles:

```text
ACTUALIZADO
EN_SINCRONIA
SIN_CAMBIO
CONFLICTO
ERROR
```

Nunca se fuerza una transicion invalida del dominio.

## 12. MVC

Cliente:

```text
/pagos/mercado-pago
```

El MVC llama a Spring Boot mediante typed `HttpClient` y `BackendBearerHandler`.

BackOffice:

```text
/backoffice/mercado-pago
```

Permite:

- visualizar pagos;
- solicitar reembolso total;
- reconciliar un pago;
- reconciliar lote.

Los POST MVC utilizan antiforgery.

Aunque el MVC pueda renderizar una accion, Spring Boot conserva la autorizacion final.

## 13. Seguridad validada

Se valido:

- checkout anonimo bloqueado;
- refund anonimo bloqueado;
- reconciliacion anonima bloqueada;
- webhook sin firma -> HTTP 401;
- webhook publico limitado al POST exacto;
- secrets no expuestos al browser;
- JWT/refresh token no expuestos al browser;
- logs sin secretos detectados;
- provider deshabilitado por defecto;
- `live_mode=true` ignorado en entorno de desarrollo.

## 14. Limitaciones deliberadas

No forman parte de Fase 11:

- produccion;
- cargos reales durante validacion;
- reembolsos parciales;
- reconciliacion programada;
- conciliacion contable externa;
- settlement/reporting financiero;
- manejo automatico de credenciales productivas.

## 15. Referencias de Mercado Pago revisadas

Documentacion revisada al cierre:

- Orders API / Crear order:
  https://www.mercadopago.com.pe/developers/es/reference/online-payments/checkout-pro/create-order/post
- Crear y configurar una order:
  https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-orders/create-order
- Reembolsos y cancelaciones:
  https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-orders/refunds-cancellations
- Webhook / firma:
  https://www.mercadopago.com.pe/developers/es/docs/checkout-api-orders/optional-notifications

## 16. Cierre

FASE 11 cubre:

```text
11.0.1  auditoria y diseno
11.0.2  configuracion TEST + provider
11.0.3  checkout CLIENTE + Orders API
11.0.4  webhook endpoint
11.0.5  validacion de firma
11.0.6  idempotencia + eventos persistentes
11.0.7  sincronizacion de estado
11.0.8  reembolso total
11.0.9  reconciliacion
11.0.10 MVC Cliente + BackOffice
11.0.11 smoke + security hardening
11.0.12 documentacion + cierre formal
```

Estado final:

```text
FASE 11 - MERCADO PAGO: COMPLETADA
```