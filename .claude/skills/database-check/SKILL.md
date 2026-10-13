---
name: database-check
description: Revisa el diseño de base de datos de Bembos Events antes de implementar o modificar SQL Server. Úsalo para validar entidades, atributos, relaciones, cardinalidades, claves, constraints, índices, tipos de datos, normalización e integridad referencial.
disable-model-invocation: true
allowed-tools: Read, Grep, Glob
---

# Database Check

Revisar el diseño de la capa de datos de **Bembos Events** antes de implementar SQL.

## Preparación

1. Leer `CLAUDE.md`.
2. Revisar documentación relacionada en `docs/`.
3. Revisar archivos existentes en `database/`.
4. Identificar exactamente qué diseño debe validarse.

No modificar archivos durante esta revisión.

## Validar entidades

Comprobar:

- que cada entidad represente un concepto real del dominio;
- que no existan entidades duplicadas;
- que las responsabilidades estén correctamente separadas;
- que no existan tablas creadas únicamente por conveniencia sin justificación.

## Validar atributos

Comprobar:

- nombre;
- propósito;
- tipo de dato;
- obligatoriedad;
- tamaño cuando corresponda;
- valores permitidos;
- duplicidad de información.

## Validar relaciones

Comprobar:

- uno a uno;
- uno a muchos;
- muchos a muchos;
- cardinalidad;
- obligatoriedad;
- dirección lógica de la relación.

Las relaciones muchos a muchos deben evaluarse para determinar si requieren una entidad intermedia.

## Validar claves

Revisar:

- PRIMARY KEY;
- FOREIGN KEY;
- UNIQUE.

Toda FK debe tener una relación de negocio justificable.

## Validar restricciones

Evaluar cuando corresponda:

- NOT NULL;
- UNIQUE;
- CHECK;
- FOREIGN KEY;
- reglas de integridad.

No depender únicamente del backend para reglas que puedan protegerse correctamente desde la base de datos.

## Dinero

Para SQL Server utilizar:

DECIMAL

Para Java utilizar:

BigDecimal

No aceptar para valores monetarios:

- FLOAT;
- REAL;
- DOUBLE;
- VARCHAR.

## Fechas y horas

Comprobar que los tipos permitan representar correctamente:

- fecha de reserva;
- hora de inicio;
- hora de fin;
- fecha de creación;
- fecha de actualización;
- pagos;
- historial.

Evitar almacenar fechas como texto.

## Reservas

Comprobar que el modelo permita validar:

- cliente;
- sede;
- salón;
- cantidad de invitados;
- capacidad;
- fecha;
- hora de inicio;
- hora de fin;
- estado;
- paquete;
- temática;
- servicios adicionales;
- importes;
- pagos.

Debe ser posible detectar superposición utilizando la regla:

inicioExistente < finNuevo
AND
finExistente > inicioNuevo

## Código público

Las reservas deben poder utilizar un código público independiente de la PK.

Ejemplo:

BEM-2026-000001

No utilizar directamente el ID interno como código público.

## Normalización

Revisar al menos:

- datos repetidos;
- dependencias innecesarias;
- listas almacenadas en una sola columna;
- grupos repetitivos;
- campos derivados innecesarios.

Priorizar un diseño normalizado sin introducir complejidad injustificada.

## Índices

Proponer índices solamente cuando exista una razón clara.

Evaluar principalmente columnas utilizadas para:

- búsqueda;
- filtros;
- joins;
- claves foráneas;
- disponibilidad;
- código público;
- correo u otros campos únicos.

No indexar todas las columnas automáticamente.

## Compatibilidad

El diseño debe ser compatible con:

- Microsoft SQL Server;
- Spring Data JPA;
- Hibernate;
- Flyway.

## Resultado

Para cada problema encontrado indicar:

- elemento afectado;
- problema;
- impacto;
- recomendación.

Clasificar cada revisión como:

- OK
- ADVERTENCIA
- ERROR

Finalizar con uno de estos estados:

DISEÑO APROBADO

o

DISEÑO REQUIERE CORRECCIONES

No crear SQL.
No modificar archivos.
No implementar correcciones automáticamente.
