---
name: phase-check
description: Verifica físicamente si una fase de Bembos Events cumple todos sus criterios antes de cerrarla o avanzar a la siguiente. Úsalo cuando el usuario solicite validar, revisar o cerrar una fase del proyecto.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Bash
---

# Phase Check

Verificar la fase indicada por el usuario antes de declararla completada.

## Procedimiento

1. Leer `CLAUDE.md`.
2. Leer `docs/ROADMAP.md` si existe.
3. Identificar la fase solicitada.
4. Identificar sus criterios de finalización.
5. Revisar físicamente los archivos relacionados.
6. Verificar estructura y contenido relevante.
7. Ejecutar build o tests solamente cuando corresponda.
8. Comparar resultados reales contra los criterios de la fase.
9. Informar cualquier requisito faltante.
10. No avanzar automáticamente a la siguiente fase.

## Regla principal

Nunca declarar una fase completada basándose únicamente en:

- intención;
- explicación;
- texto generado;
- archivos supuestamente creados;
- resultados no verificados.

Debe existir evidencia física en el repositorio.

## Verificación de archivos

Cuando la fase requiera archivos:

- comprobar que existen;
- comprobar que no estén vacíos;
- comprobar que estén en la ruta correcta;
- leer su contenido cuando sea necesario;
- detectar rutas duplicadas o incorrectas.

## Verificación de código

Cuando exista código compilable:

Backend:

- utilizar Maven cuando exista el proyecto Spring Boot.

Frontend:

- utilizar `dotnet build` cuando exista ASP.NET Core.

No ejecutar builds de componentes que todavía no existen.

## Resultado

Clasificar cada criterio como:

- OK
- PENDIENTE
- ERROR
- NO APLICA

Después indicar únicamente uno de estos estados:

FASE APROBADA

o

FASE NO APROBADA

Si no está aprobada, indicar exactamente qué falta.

No corregir automáticamente los problemas salvo solicitud explícita del usuario.
