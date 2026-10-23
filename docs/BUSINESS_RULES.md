# Reglas de Negocio - Bembos Events

Este documento contiene las reglas funcionales que deben respetar backend, frontend, integraciones e IA.

# 1. Usuarios

Roles oficiales:

- CLIENTE.
- ENCARGADO_LOCAL.
- ADMINISTRADOR.

## CLIENTE

Reglas:

- Puede registrarse mediante el registro publico.
- El registro publico siempre crea un CLIENTE.
- No puede elegir su propio rol.
- No puede asignarse una sede administrativa.
- No tiene acceso al BackOffice.

## ENCARGADO_LOCAL

Reglas:

- Tiene acceso administrativo limitado.
- Debe estar relacionado con una sede mediante UsuarioSede.
- Solo puede consultar y operar recursos de su sede.
- No obtiene acceso global por tener rol administrativo.

## ADMINISTRADOR

Reglas:

- Tiene acceso administrativo global.
- Puede administrar usuarios.
- Puede crear ENCARGADO_LOCAL.
- Puede asignar sede a ENCARGADO_LOCAL.
- Puede consultar todas las sedes.
- No necesita UsuarioSede para obtener alcance global.

# 2. UsuarioSede

UsuarioSede representa el alcance territorial de un usuario administrativo.

Regla conceptual:

```text
Rol        = que puede hacer
UsuarioSede = donde puede hacerlo
```

Para el MVP:

- ENCARGADO_LOCAL tiene una sede asignada.
- Una sede puede tener varios encargados.

# 3. Clientes

Todo Usuario que posea el rol CLIENTE debe disponer de un perfil Cliente.

Un Usuario no puede representar multiples perfiles Cliente.

Reglas de ciclo de vida:

- registro publico: crea Usuario, UsuarioRol CLIENTE y Cliente activo.
- ADMIN crea CLIENTE: crea tambien el perfil Cliente.
- cambio de otro rol a CLIENTE: crea el perfil si no existe o reactiva el existente.
- cambio de CLIENTE a otro rol: conserva el perfil historico y lo marca inactivo.
- el perfil Cliente no se elimina fisicamente por un cambio de rol.

# 4. Reservas

## Codigo de reserva

Formato esperado:

```text
BEM-AAAA-NNNNNN
```

Ejemplo:

```text
BEM-2026-000001
```

El codigo debe ser unico.

## Capacidad

La cantidad de invitados nunca puede superar la capacidad del salon.

Regla:

```text
cantidadInvitados <= capacidadSalon
```

## Horarios

La hora de inicio debe ser anterior a la hora de fin.

El salon debe estar disponible para el horario solicitado.

## Superposicion de reservas

Existe conflicto cuando:

```text
inicioExistente < finNuevo
AND
finExistente > inicioNuevo
```

Una reserva no puede confirmarse sobre un horario ya ocupado por otra reserva incompatible.

## Monto minimo

El monto minimo es configurable.

Valor de desarrollo actual:

```text
S/ 500.00
```

La configuracion pertenece al backend.

## Deposito

El porcentaje de deposito es configurable.

Valor de desarrollo actual:

```text
50%
```

El backend calcula el monto correspondiente.

## Precios

El frontend no calcula el precio definitivo.

El backend es responsable de calcular:

- monto base.
- servicios adicionales.
- monto total.
- porcentaje de deposito.
- monto de deposito.

# 5. Estados de Reserva

Estados oficiales:

- PENDIENTE.
- CONFIRMADA.
- EN_PROCESO.
- COMPLETADA.
- CANCELADA.
- EXPIRADA.

Toda transicion debe ser validada por backend.

El frontend puede solicitar una accion, pero no determinar si la transicion es legal.

# 6. Pagos

Estados oficiales:

- PENDIENTE.
- APROBADO.
- RECHAZADO.
- CANCELADO.
- REEMBOLSADO.

Toda transicion de estado debe validarse por backend.

La validacion de transiciones se ejecuta internamente en el backend.

La validacion se ejecuta internamente durante el cambio real de estado.

## Pagos base - FASE 7.6F

Reglas implementadas:

- Cada Pago pertenece a una Reserva.
- Una Reserva puede tener multiples Pagos.
- Se permiten pagos parciales.
- `Pago.monto` debe ser mayor que 0.
- `metodoPago` es obligatorio.
- no existe todavia un catalogo cerrado de metodos de pago.
- un Pago nuevo inicia en `PENDIENTE`.
- `fechaPago` puede permanecer `null` mientras el pago no haya sido confirmado.
- `referenciaExterna` permanece disponible para integraciones futuras.
- el Cliente solamente opera Pagos asociados a sus propias Reservas.
- un recurso ajeno se trata como no encontrado.
- ADMINISTRADOR dispone de alcance global en BackOffice.
- ENCARGADO_LOCAL solamente consulta Pagos de Reservas pertenecientes a su sede.
- el frontend no envia `idCliente` ni `idSede` como fuente de autorizacion.
- no existe un endpoint final generico para que el navegador marque un Pago como `APROBADO`.
- `validar-transicion` continua temporalmente como endpoint tecnico hasta FASE 7.6G.
- Mercado Pago fue implementado y cerrado en FASE 11.
# 7. Pagos externos - Mercado Pago

Reglas implementadas en Fase 11:

- proveedor integrado: Mercado Pago Checkout Pro + Orders API;
- configuracion de desarrollo/test;
- un checkout crea un Pago local `PENDIENTE`;
- el metodo del checkout es controlado por backend: `MERCADO_PAGO`;
- `Pago.referenciaExterna` almacena el Order id;
- el browser no aprueba pagos;
- el webhook debe superar validacion de firma;
- los eventos son idempotentes;
- eventos ya procesados se ignoran;
- eventos fallidos antes de procesarse pueden reintentarse;
- el estado externo se consulta desde la Order;
- solamente las transiciones validas del dominio modifican el Pago;
- `processed/accredited` puede llevar `PENDIENTE -> APROBADO`;
- `failed` puede llevar `PENDIENTE -> RECHAZADO`;
- `canceled` puede llevar `PENDIENTE -> CANCELADO`;
- un reembolso confirmado puede llevar `APROBADO -> REEMBOLSADO`;
- estados externos no finales no fuerzan cambio;
- un estado externo incompatible se reporta como conflicto durante reconciliacion;
- reembolso total requiere Pago `APROBADO`;
- reembolso total solamente puede ser solicitado por ADMINISTRADOR;
- reembolso parcial queda fuera de Fase 11;
- reconciliacion manual individual y lote maximo de 100;
- no existe reconciliacion programada en Fase 11;
- provider deshabilitado por defecto;
- `live_mode=true` se ignora en este entorno de desarrollo.

Documento detallado:

`docs/MERCADO_PAGO.md`

# 8. Acceso por sede

ADMINISTRADOR:

```text
puede acceder a todas las sedes
```

ENCARGADO_LOCAL:

```text
puede acceder solamente a su sede
```

CLIENTE:

```text
no tiene acceso administrativo a sedes
```

Esta regla ya se encuentra aplicada a las Reservas del BackOffice.

Para Reservas:

- ADMINISTRADOR dispone de alcance global.
- ENCARGADO_LOCAL obtiene su sede desde UsuarioSede.
- ENCARGADO_LOCAL solamente lista reservas de su sede.
- ENCARGADO_LOCAL solamente consulta reservas de su sede.
- ENCARGADO_LOCAL solamente modifica reservas de su sede.
- un identificador o parametro enviado por el frontend nunca amplia el alcance administrativo.

La regla de alcance por sede se encuentra aplicada actualmente a:

- Sedes BackOffice.
- Reservas BackOffice.
- Pagos BackOffice.

Para ENCARGADO_LOCAL, la sede autorizada se obtiene desde UsuarioSede y nunca desde un parametro enviado por el frontend.

Un recurso perteneciente a otra sede se trata como no visible y responde 404 cuando corresponde evitar revelar su existencia.

# 9. Chat - planeado

Flujo objetivo:

```text
CLIENTE
   |
   v
IA
   |
   +---- resuelve
   |
   +---- escala
           |
           v
     ENCARGADO_LOCAL
```

Estados propuestos:

- BOT.
- ESPERANDO_ENCARGADO.
- EN_ATENCION.
- CERRADO.

Al escalar a una persona debe mantenerse la misma conversacion.

Si la conversacion corresponde a una sede, solo debe entregarse a encargados de esa sede.

# 10. Inteligencia Artificial - planeado

La IA puede ayudar a:

- consultar sedes.
- consultar salones.
- consultar paquetes.
- consultar tematicas.
- consultar servicios.
- consultar disponibilidad mediante backend.
- recomendar opciones.
- preparar un borrador de reserva.
- responder preguntas.
- escalar a un encargado.

La IA nunca decide definitivamente:

- precios.
- disponibilidad.
- capacidad.
- permisos.
- aprobacion de pagos.
- transiciones de estado.

La IA nunca accede directamente a SQL Server.

# 11. Historial

Los cambios importantes de una reserva deben poder quedar registrados.

Ejemplos:

- reserva creada.
- cambio de estado.
- pago registrado.
- pago aprobado.
- cancelacion.
- usuario responsable.

# 12. Regla de autoridad

La fuente definitiva de verdad para las reglas de negocio es el backend.

Ni frontend, Postman, IA ni una integracion externa pueden saltarse las validaciones de Services.

<!-- PHASE12_ADVANCED_AI_BUSINESS_START -->
# IA avanzada - reglas de negocio

1. La IA no reemplaza reglas de dominio.
2. Disponibilidad siempre se consulta mediante `DisponibilidadSalonService`.
3. Recomendaciones son referenciales, no cotizaciones finales.
4. Un borrador conversacional no es una Reserva.
5. La confirmacion debe ser explicita.
6. La confirmacion final delega en `ReservaClienteService`.
7. Una falla de revalidacion deja el borrador abierto.
8. La IA no puede aprobar Pagos.
9. RAG solo aporta contexto publico read-only.
10. Los resultados de tools de negocio se renderizan deterministicamente.
11. `CHAT_GENERAL` es la unica ruta al provider.
12. Los insights BackOffice se calculan desde metricas; no son texto inventado por un LLM.

Detalle: `docs/AI_ADVANCED.md`.
<!-- PHASE12_ADVANCED_AI_BUSINESS_END -->
