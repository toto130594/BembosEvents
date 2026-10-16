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

El endpoint tecnico validar-transicion existe actualmente para probar esta regla.

En la API final la validacion debera ejecutarse internamente durante el cambio real de estado.

# 7. Pagos externos - planeado

Cuando se integre Mercado Pago:

- El backend no confiara solamente en la respuesta del navegador.
- Los webhooks deberan validarse.
- La firma del proveedor debera comprobarse cuando corresponda.
- Las operaciones deberan ser idempotentes.
- Un mismo evento no podra procesarse dos veces.
- El identificador externo del proveedor debera permitir detectar duplicados.

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

Esta regla debera aplicarse tambien a reservas, pagos y demas recursos del BackOffice.

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
