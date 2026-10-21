# Chat e Inteligencia Artificial - Bembos Events

## Estado

IMPLEMENTADO EN FASE 10.
La implementacion base de Chat e IA fue completada en FASE 10.
## Objetivo

Permitir que un cliente converse inicialmente con una IA y pueda ser transferido posteriormente a un encargado humano.

Flujo objetivo:

```text
CLIENTE
   |
   v
CHAT
   |
   v
IA
   |
   +---- Resuelve consulta
   |
   +---- No puede resolver
              |
              v
      ESCALAR A HUMANO
              |
              v
       ENCARGADO_LOCAL
```

## Principio principal

La IA no es la fuente de verdad del negocio.

La IA utiliza capacidades controladas del backend.

## Acceso a datos

Flujo permitido:

```text
IA
 |
 v
Spring Boot API / Services
 |
 v
Reglas de negocio
 |
 v
Repositories
 |
 v
SQL Server
```

Flujo prohibido:

```text
IA
 |
 v
SQL Server directo
```

## Funciones que podra utilizar la IA

La IA podra consultar mediante backend:

- sedes.
- salones.
- capacidad.
- paquetes.
- tematicas.
- servicios adicionales.
- horarios.
- disponibilidad.

Podra ayudar al cliente a encontrar opciones.

Ejemplo:

```text
Cliente:
Quiero una fiesta para 40 personas en San Miguel.

IA:
interpreta cantidad y sede
        |
        v
consulta backend
        |
        v
presenta opciones reales
```

## Operaciones que la IA no decide

La IA no puede determinar definitivamente:

- precio final.
- disponibilidad final.
- capacidad valida.
- permisos.
- estado de pago.
- aprobacion de pago.
- transicion de reserva.
- transicion de pago.

Estas decisiones pertenecen a Services.

## Creacion asistida de reserva

En una fase avanzada la IA podra recopilar informacion como:

- sede.
- fecha.
- hora.
- cantidad de invitados.
- paquete.
- tematica.
- servicios.

La IA podra construir una solicitud o borrador.

La creacion definitiva de la reserva debera pasar por ReservaService.

## Escalamiento a encargado

Cuando el cliente solicite atencion humana o la IA no pueda resolver una consulta:

```text
BOT
 |
 v
ESPERANDO_ENCARGADO
 |
 v
EN_ATENCION
 |
 v
CERRADO
```

## Seleccion del encargado

Cuando una conversacion tenga una sede:

```text
Conversacion
     |
   idSede
     |
     v
UsuarioSede
     |
     v
ENCARGADO_LOCAL
```

La conversacion no debe asignarse a un encargado de otra sede.

## Continuidad

Cuando la IA transfiere la conversacion a una persona:

- no se crea un chat nuevo.
- no se pierde el historial.
- el encargado puede leer el contexto anterior.

## Modelo de datos propuesto

### Conversacion

Campos candidatos:

- idConversacion.
- idCliente.
- idSede.
- idUsuarioAsignado.
- estado.
- canal.
- fechaInicio.
- fechaCierre.

### MensajeChat

Campos candidatos:

- idMensaje.
- idConversacion.
- tipoEmisor.
- idUsuario.
- mensaje.
- fechaEnvio.
- leido.

## Tipos de emisor

- CLIENTE.
- IA.
- ENCARGADO.
- ADMINISTRADOR.

## Tiempo real

Se evaluara utilizar WebSocket para actualizaciones de mensajes en tiempo real.

## Notificaciones

Eventos futuros:

- nueva conversacion.
- chat escalado.
- nuevo mensaje.
- encargado asignado.

Estos eventos podran generar:

- notificacion dentro del BackOffice.
- correo.
- WhatsApp.
- push.

## Modelo de IA

La arquitectura no debe depender directamente de un proveedor unico.

El sistema podra utilizar diferentes proveedores o modelos mientras respeten el contrato del backend.

Durante desarrollo se podra evaluar Ollama.

## Seguridad

La IA debe respetar exactamente las mismas reglas de acceso que cualquier otro consumidor.

Nunca debe recibir privilegios administrativos por ser un componente interno.

## Futuro

Funciones avanzadas posibles:

- recomendaciones de paquetes.
- busqueda por lenguaje natural.
- disponibilidad desde chat.
- borrador de reserva.
- preguntas frecuentes mediante RAG.
- analisis administrativo.
- resumen de conversaciones para encargado.

<!-- PHASE10_AI_CHAT_FINAL_START -->
## Cierre implementacion base FASE 10 - 2026-10-21

Estado: IMPLEMENTADO.

### Flujo implementado

```text
CLIENTE
   |
   v
BOT / IA
   |
   +---- responde consulta general
   |
   +---- escalamiento explicito
           |
           v
ESPERANDO_ENCARGADO
           |
           v
ENCARGADO_LOCAL de la sede
           |
           v
EN_ATENCION
```

Estados disponibles:

- BOT.
- ESPERANDO_ENCARGADO.
- EN_ATENCION.
- CERRADO.

### Persistencia

- Conversacion y MensajeChat se persisten en SQL Server mediante JPA.
- El historial reutiliza MensajeChat; no existe una tabla de historial duplicada.
- Mensajes soportados: CLIENTE, IA, ENCARGADO_LOCAL y tipos ya definidos por el dominio.

### IA

- ChatAiProvider define la abstraccion del proveedor.
- BasicChatAiProvider funciona sin credenciales externas.
- OpenAiCompatibleChatAiProvider es opcional y configurable.
- La IA solamente responde mientras la conversacion esta en BOT.
- La IA no accede directamente a SQL Server.
- La IA no posee privilegios especiales ni aprueba pagos.

Configuracion:

```properties
app.chat.ai.provider=${AI_CHAT_PROVIDER:basic}
app.chat.ai.base-url=${AI_CHAT_BASE_URL:http://host.docker.internal:11434/v1}
app.chat.ai.api-key=${AI_CHAT_API_KEY:}
app.chat.ai.model=${AI_CHAT_MODEL:qwen3:8b}
app.chat.ai.temperature=${AI_CHAT_TEMPERATURE:0.2}
```

### Realtime

- Handshake Spring WebSocket/STOMP: /ws/chat.
- Application prefix: /app.
- Topic por conversacion: /topic/chat/conversaciones/{idConversacion}.
- Notificacion global BackOffice: /topic/backoffice/chat/notificaciones.
- Notificacion por sede: /topic/backoffice/chat/sedes/{idSede}/notificaciones.
- STOMP CONNECT requiere autenticacion.
- SUBSCRIBE valida ownership, rol y sede.
- SEND directo a /topic/** esta bloqueado.
- SEND permitido solamente por /app/chat/conversaciones/{idConversacion}/mensajes y con ownership CLIENTE.

### MVC

- El browser no recibe JWT backend.
- El browser no recibe refresh token backend.
- ASP.NET Core MVC conserva los tokens server-side.
- El browser no conecta directamente a /ws/chat.
- La UI MVC actualiza historial mediante polling same-origin.

### Limites de FASE 10

- No se implementaron heuristicas automaticas de intent detection para escalamiento.
- No se implemento bandeja persistente de notificaciones leido/no-leido.
- Disponibilidad conversacional, recomendaciones, borradores de reserva, RAG, analitica y dashboard inteligente permanecen en FASE 12.
<!-- PHASE10_AI_CHAT_FINAL_END -->
