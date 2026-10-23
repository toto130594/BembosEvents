# IA avanzada - Bembos Events

Estado: **FASE 12 COMPLETADA**.

Fecha de cierre tecnico/documental: 2026-10-22.

## 1. Objetivo

FASE 12 extiende el chat de FASE 10 con capacidades de IA avanzadas sin otorgar autoridad especial al modelo.

Principio central:

```text
la IA interpreta y asiste;
Spring Boot autentica, autoriza, valida y ejecuta reglas de negocio.
```

## 2. Arquitectura

```text
Browser
  |
  v
ASP.NET Core MVC
  |
  | same-origin / Session server-side
  v
Spring Boot REST
  |
  +--> AiSecurityGuard
  |
  +--> AiIntentInterpreter
  |
  +--> AiConversationOrchestrator
           |
           +--> AiToolExecutor
           |      |
           |      +--> AiToolRegistry allowlist
           |              |
           |              +--> CONSULTAR_DISPONIBILIDAD
           |              +--> RECOMENDAR_CATALOGO
           |              +--> ACTUALIZAR_BORRADOR_RESERVA
           |              +--> CONFIRMAR_BORRADOR_RESERVA
           |              +--> CONSULTAR_CONOCIMIENTO
           |
           +--> ChatAiProvider
                  |
                  +--> solo CHAT_GENERAL
```

No existe ejecucion dinamica de tools por nombre proveniente del usuario o del modelo.

No se requiere native function calling del proveedor.

## 3. Disponibilidad conversacional

La IA no inventa disponibilidad.

`AiAvailabilityTool` delega en `DisponibilidadSalonService`.

Antes de ejecutar:

- valida identidad CLIENTE;
- valida propiedad de la conversacion;
- exige conversacion en estado BOT;
- valida salon publico activo;
- valida consistencia de sede;
- reutiliza la regla deterministica de disponibilidad.

## 4. Recomendaciones

Las recomendaciones son deterministicas y explicables.

Orden de paquetes:

1. coincidencias con preferencias;
2. cercania entre invitados solicitados y cantidad base;
3. precio;
4. id.

Tematicas y servicios solo se recomiendan cuando existe coincidencia de preferencia.

La recomendacion no es una cotizacion final ni crea una Reserva.

## 5. Borrador conversacional

Persistencia server-side:

```text
Conversacion
   1
   |
   1
BorradorReservaConversacional
   1
   |
   N
BorradorReservaServicio
```

Migracion: V13.

El borrador:

- pertenece al CLIENTE autenticado y a una Conversacion;
- no es una Reserva;
- permite actualizaciones incrementales;
- permite limpiar paquete, tematica y servicios;
- permanece ABIERTO si la confirmacion final falla.

La confirmacion real solo ocurre con una frase explicita reconocida.

La confirmacion final delega en `ReservaClienteService`, que vuelve a validar:

- sede;
- salon;
- capacidad;
- disponibilidad;
- paquete;
- tematica;
- servicios;
- reglas de monto;
- reglas del dominio.

## 6. RAG

Contrato: `KnowledgeRetriever`.

Implementacion actual: recuperacion lexica deterministica sobre corpus publico allowlisted en classpath.

No se indexan documentos internos como:

- SECURITY;
- ARCHITECTURE;
- DECISIONS;
- configuracion;
- `.env`;
- secretos.

Las respuestas con evidencia incluyen source URI `kb://...`.

Si no existe evidencia suficiente, no se fabrica evidencia.

Vector DB y embeddings no son requeridos en esta fase.

## 7. Analitica de IA

Migracion: V14.

Entidad: `AiAnalyticsEvent`.

Granularidad: un evento por turno orquestado.

Se captura:

- conversacion;
- sede cuando aplica;
- intent;
- tool cuando aplica;
- ruta TOOL o PROVIDER;
- exito/error;
- duracion;
- tipo de error por clase.

No se persiste:

- prompt;
- respuesta;
- email;
- payload completo del tool;
- fragmentos RAG;
- tokens;
- secretos.

La persistencia usa transaccion independiente y una falla de analitica no rompe el chat.

## 8. Dashboard inteligente BackOffice

Endpoint:

```http
GET /api/backoffice/ai/dashboard
```

Roles:

- ADMINISTRADOR;
- ENCARGADO_LOCAL.

Reglas:

- ADMINISTRADOR puede consultar global o filtrar sede;
- ENCARGADO_LOCAL queda limitado a su sede mediante `SedeAccessService`;
- ventana maxima: 31 dias;
- solo lectura.

Metricas:

- total;
- exito/error;
- error rate;
- latencia promedio y maxima;
- intents;
- tools;
- TOOL vs PROVIDER.

Los insights son deterministas y derivados de metricas. No se usa un LLM para inventar conclusiones.

## 9. Seguridad IA

`AiSecurityGuard` se ejecuta antes del interprete.

Controles:

- longitud maxima por defecto: 2000 caracteres;
- rate limit por cliente + conversacion: 20 turnos / 60 s;
- presupuesto de provider: 30 llamadas / 3600 s;
- sujetos de rate/cost almacenados como SHA-256;
- prompt injection de alta confianza bloqueado;
- disclosure de system/developer prompt bloqueado;
- solicitudes de shell, PowerShell, cmd, bash, SQL o filesystem bloqueadas;
- bypass de tool allowlist bloqueado.

El presupuesto de provider aplica solamente a `CHAT_GENERAL`, no a tools deterministas.

El control de costo se basa en cantidad de invocaciones, no en tokens, porque `ChatAiProvider` no expone usage.

Limitaciones actuales:

- contadores en memoria;
- no son distribuidos entre replicas;
- se reinician al reiniciar el proceso;
- no se afirma costo monetario exacto.

## 10. Provider

`ChatAiProvider` se conserva compatible con FASE 10.

El proveedor:

- recibe el texto del usuario como contenido no confiable;
- no puede elegir tools;
- no puede otorgarse autoridad;
- no puede aprobar pagos;
- no puede crear reservas por fuera de los servicios deterministas;
- no puede revelar secretos, tokens o configuracion por politica.

Los resultados de tools de negocio se renderizan deterministicamente sin pasar por el provider.

## 11. MVC conversacional

Se reutiliza la pantalla de chat existente.

Cuando la conversacion esta en BOT:

- se muestra guia de capacidades;
- sugerencias solo rellenan el textarea;
- no existe auto-submit;
- maxlength = 2000;
- existe contador de caracteres;
- una frase explicita de confirmacion muestra advertencia antes del envio.

El browser:

- no recibe backend JWT;
- no recibe refresh token;
- no llama directo a `backend:8080`;
- no conecta directo a `/ws/chat`;
- conserva same-origin MVC polling.

La advertencia del browser no es autoridad de negocio.

## 12. E2E y failure modes

Cierre 12.0.11 validado:

- CLIENTE temporal auto-registrado;
- login real;
- acceso anonimo bloqueado;
- conversacion BOT creada;
- RAG con `kb://`;
- recomendacion deterministica;
- disponibilidad deterministica;
- borrador parcial;
- confirmacion ambigua no crea Reserva;
- prompt injection bloqueado;
- mensaje mayor a 2000 bloqueado;
- conversacion desconocida/no propia bloqueada;
- historial CLIENTE + IA persistido;
- cero Reservas reales creadas;
- cero operaciones de pago.

Contrato observado del envio de turno:

```text
POST /api/chat/conversaciones/{id}/mensajes
HTTP 201 Created
```

## 13. Migraciones

FASE 12 utiliza:

- V13: borrador conversacional;
- V14: analitica IA.

FASE 12 no modifica V11 ni V12.

## 14. Secuencia cerrada

```text
12.0.1  audit + freeze
12.0.2  typed tools + availability
12.0.3  recommendation engine
12.0.4  server-side conversational drafts
12.0.5  RAG knowledge + attribution
12.0.6  orchestration + safe tool execution
12.0.7  analytics event
12.0.8  intelligent BackOffice dashboard
12.0.9  prompt-injection + rate/cost/security hardening
12.0.10 MVC conversational UX
12.0.11 E2E + failure modes
12.0.12 documentation + formal close
```

Estado final:

```text
FASE 12 - IA AVANZADA: COMPLETADA
```