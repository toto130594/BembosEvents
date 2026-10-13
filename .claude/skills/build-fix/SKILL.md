---
name: build-fix
description: Diagnostica y corrige errores reales de compilación o pruebas en Bembos Events. Úsalo cuando Maven, Spring Boot, dotnet build o los tests fallen y sea necesario localizar la causa raíz, aplicar una corrección mínima y verificar nuevamente.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob, Edit, Bash
---

# Build Fix

Diagnosticar errores reales de compilación o pruebas y corregir únicamente la causa necesaria.

## Flujo

1. Ejecutar el build o test que falla.
2. Leer literalmente el error.
3. Localizar el archivo relacionado.
4. Identificar la causa raíz.
5. Aplicar la corrección mínima.
6. Ejecutar nuevamente el mismo comando.
7. Repetir solo si continúa fallando.

Flujo obligatorio:

BUILD
→ ERROR
→ READ
→ FIX
→ BUILD

## Backend

Cuando exista Spring Boot usar Maven según corresponda:

- `mvn test`
- `mvn clean test`
- `mvn clean verify`

No ejecutar Maven si el backend todavía no existe.

## Frontend

Cuando exista ASP.NET Core usar según corresponda:

- `dotnet build`
- `dotnet test`

No ejecutar comandos frontend si el proyecto todavía no existe.

## Reglas

No:

- inventar dependencias;
- eliminar tests porque fallen;
- ocultar errores;
- cambiar arquitectura para evitar el error;
- agregar funcionalidades nuevas;
- modificar componentes no relacionados;
- declarar éxito sin volver a ejecutar la verificación.

## Resultado

Al finalizar indicar:

- error inicial;
- causa raíz;
- archivo modificado;
- corrección aplicada;
- comando de verificación;
- resultado final.

Solo declarar `BUILD OK` cuando la verificación real finalice correctamente.
