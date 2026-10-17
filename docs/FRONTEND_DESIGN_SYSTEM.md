# Frontend Design System - Bembos Events

## Estado

FASE 8.0.2.

Este documento define el contrato visual del frontend ASP.NET Core MVC de Bembos Events.

El objetivo es construir una experiencia visual inspirada en el lenguaje de marca de Bembos, adaptada a una plataforma de reservas de fiestas y eventos.

No se busca copiar el HTML, CSS ni assets del sitio comercial de Bembos.

Se reutilizan conceptos visuales generales:

- azul como color principal.
- rojo como color de accion.
- amarillo como color de acento.
- fotografia de alto impacto.
- botones grandes.
- tarjetas promocionales.
- titulos fuertes.
- navegacion clara.
- enfoque mobile-first.

Los logos, fotografias y otros recursos protegidos solamente deben utilizarse cuando exista autorizacion para ello.

---

# 1. Principios visuales

## 1.1 Energia

Bembos Events debe sentirse:

- divertido.
- juvenil.
- dinamico.
- familiar.
- accesible.
- moderno.

La interfaz publica puede utilizar formas, imagenes y acentos llamativos.

El BackOffice debe conservar los colores de marca, pero con una presentacion mas sobria y orientada a productividad.

## 1.2 Claridad

Cada pantalla debe tener una accion principal claramente identificable.

Ejemplos:

- Reservar fiesta.
- Ver paquete.
- Elegir sede.
- Continuar.
- Confirmar reserva.
- Ver detalle.

## 1.3 Mobile first

La reserva debe poder completarse comodamente desde un telefono.

El layout nunca debe depender exclusivamente de desktop.

## 1.4 Backend como autoridad

La capa visual nunca decide:

- permisos.
- disponibilidad.
- capacidad.
- precios definitivos.
- monto de deposito.
- estados.
- transiciones.
- acceso por sede.

Estas decisiones pertenecen al backend Spring Boot.

---

# 2. Arquitectura visual

La aplicacion tendra dos superficies principales.

## 2.1 Portal publico / Cliente

Orientado a:

- descubrir paquetes.
- consultar sedes.
- consultar salones.
- explorar tematicas.
- explorar servicios adicionales.
- registrarse.
- iniciar sesion.
- crear reservas.
- consultar reservas.
- consultar pagos.

Debe ser altamente visual.

## 2.2 BackOffice

Orientado a:

- ADMINISTRADOR.
- ENCARGADO_LOCAL.

Debe priorizar:

- legibilidad.
- tablas.
- filtros.
- estados.
- acciones.
- productividad.

Debe conservar la identidad de Bembos Events sin parecer una tienda ecommerce.

---

# 3. Paleta de trabajo

La siguiente paleta es una aproximacion inspirada en el lenguaje visual de Bembos.

No representa una declaracion de valores oficiales de marca.

## Primarios

| Token | Valor | Uso |
|---|---|---|
| `--be-blue-900` | `#003B71` | Header, footer, titulos |
| `--be-blue-700` | `#00529B` | Botones secundarios, enlaces |
| `--be-blue-500` | `#1479C9` | Hover y componentes secundarios |
| `--be-red-600` | `#E1251B` | CTA principal |
| `--be-red-700` | `#BD1D16` | Hover CTA |
| `--be-yellow-400` | `#FFD428` | Badges y acentos |
| `--be-yellow-300` | `#FFE26B` | Fondos de destaque |

## Neutros

| Token | Valor |
|---|---|
| `--be-white` | `#FFFFFF` |
| `--be-gray-50` | `#F8F9FA` |
| `--be-gray-100` | `#F1F3F5` |
| `--be-gray-200` | `#E5E7EB` |
| `--be-gray-500` | `#6B7280` |
| `--be-gray-700` | `#374151` |
| `--be-gray-900` | `#171717` |

## Estados

| Estado | Valor |
|---|---|
| Exito | `#198754` |
| Advertencia | `#F59E0B` |
| Error | `#DC3545` |
| Informacion | `#0D6EFD` |

---

# 4. Variables CSS base

La implementacion futura debe centralizar tokens.

```css
:root {
    --be-blue-900: #003B71;
    --be-blue-700: #00529B;
    --be-blue-500: #1479C9;

    --be-red-600: #E1251B;
    --be-red-700: #BD1D16;

    --be-yellow-400: #FFD428;
    --be-yellow-300: #FFE26B;

    --be-white: #FFFFFF;

    --be-gray-50: #F8F9FA;
    --be-gray-100: #F1F3F5;
    --be-gray-200: #E5E7EB;
    --be-gray-500: #6B7280;
    --be-gray-700: #374151;
    --be-gray-900: #171717;

    --be-success: #198754;
    --be-warning: #F59E0B;
    --be-danger: #DC3545;
    --be-info: #0D6EFD;

    --be-radius-sm: 8px;
    --be-radius-md: 14px;
    --be-radius-lg: 22px;
    --be-radius-pill: 999px;

    --be-shadow-sm:
        0 2px 8px rgba(0, 0, 0, 0.08);

    --be-shadow-md:
        0 8px 24px rgba(0, 0, 0, 0.12);

    --be-container: 1200px;
}
```

Nunca deben dispersarse colores hardcodeados innecesariamente por las Views.

---

# 5. Tipografia

## Texto general

Familia objetivo:

```css
font-family:
    Inter,
    Arial,
    Helvetica,
    sans-serif;
```

Uso:

- formularios.
- tablas.
- descripciones.
- menu.
- contenido BackOffice.

## Titulos promocionales

Familia objetivo:

```css
font-family:
    "Barlow Condensed",
    "Arial Narrow",
    Arial,
    sans-serif;
```

Uso:

- hero.
- banners.
- encabezados de campana.
- promociones.

Debe utilizarse peso alto.

Ejemplo:

```css
font-weight: 800;
text-transform: uppercase;
```

La aplicacion debe funcionar correctamente aunque una fuente web externa no pueda cargarse.

---

# 6. Escala tipografica

| Token | Desktop | Mobile |
|---|---:|---:|
| Hero | 56px | 36px |
| H1 | 44px | 32px |
| H2 | 34px | 28px |
| H3 | 26px | 22px |
| Body large | 18px | 17px |
| Body | 16px | 16px |
| Small | 14px | 14px |
| Caption | 12px | 12px |

---

# 7. Espaciado

Se utilizara una escala basada en 4px.

```text
4
8
12
16
20
24
32
40
48
64
80
```

Los componentes no deben utilizar margenes arbitrarios si un valor de la escala puede resolverlo.

---

# 8. Contenedor

Desktop:

```css
max-width: 1200px;
margin-inline: auto;
padding-inline: 24px;
```

Tablet:

```css
padding-inline: 20px;
```

Mobile:

```css
padding-inline: 16px;
```

---

# 9. Breakpoints

Referencia inicial:

```text
Mobile       < 576px
Mobile large >= 576px
Tablet       >= 768px
Desktop      >= 992px
Wide         >= 1200px
```

La experiencia debe disenarse primero para mobile.

---

# 10. Botones

## CTA principal

Uso:

- Reservar.
- Continuar.
- Confirmar.

Estilo:

```text
Fondo: rojo
Texto: blanco
Peso: fuerte
Radio: pill o 14px
Altura minima: 48px
```

Ejemplo conceptual:

```text
[ RESERVA TU FIESTA ]
```

## Secundario

Uso:

- Ver detalle.
- Ver sede.
- Volver.

Estilo:

```text
Fondo: azul
Texto: blanco
```

## Outline

Uso:

- acciones secundarias.
- cancelar navegacion.
- filtros.

Estilo:

```text
Fondo: transparente
Borde: azul
Texto: azul
```

## Estados obligatorios

Todo boton debe contemplar:

- normal.
- hover.
- focus-visible.
- disabled.
- loading cuando corresponda.

---

# 11. Header publico

Desktop:

```text
+--------------------------------------------------------------+
| LOGO | Ubicacion | Menu | Buscar | Mi cuenta | Mi reserva    |
+--------------------------------------------------------------+
```

Navegacion objetivo:

```text
Inicio
Paquetes
Tematicas
Sedes
Salones
Servicios
```

Acciones:

```text
Mi cuenta
Mi reserva
```

El header debe permanecer visualmente simple.

No debe convertirse en un mega-menu en la primera version.

---

# 12. Header mobile

Debe utilizar:

```text
Logo
Menu hamburguesa
Cuenta
Reserva
```

La navegacion completa aparecera en drawer o menu colapsable.

Los targets tactiles deben tener al menos aproximadamente 44px.

---

# 13. Hero

La Home debe comenzar con una pieza visual dominante.

Estructura:

```text
+----------------------------------------------------------+
| TITULO PROMOCIONAL                                      |
|                                                        |
| Texto corto                         Imagen / fotografia  |
|                                                        |
| [ RESERVA TU FIESTA ]                                  |
+----------------------------------------------------------+
```

Caracteristicas:

- titulo grande.
- poco texto.
- imagen protagonista.
- CTA inmediato.
- fondo de marca.
- adaptacion completa a mobile.

---

# 14. Cards

Todas las cards deben derivar de un patron comun.

## Card de paquete

Contenido:

```text
Imagen
Badge opcional
Nombre
Descripcion corta
Cantidad de invitados
Precio desde
CTA
```

## Card de sede

Contenido:

```text
Imagen
Nombre
Distrito
Direccion
Cantidad de salones
CTA
```

## Card de tematica

Contenido:

```text
Imagen
Nombre
Descripcion
CTA
```

## Card de servicio adicional

Contenido:

```text
Imagen o icono
Nombre
Descripcion
Precio cuando el backend lo proporcione
Selector
```

---

# 15. Patron visual de card

```css
border-radius: var(--be-radius-lg);
background: var(--be-white);
box-shadow: var(--be-shadow-sm);
overflow: hidden;
```

Hover desktop:

```text
elevacion suave
```

No utilizar transformaciones agresivas.

---

# 16. Badges

Ejemplos:

```text
MAS ELEGIDO
NUEVO
PROMO
DISPONIBLE
CONFIRMADA
PENDIENTE
```

Los estados de negocio nunca deben depender solamente del color.

Siempre deben incluir texto.

---

# 17. Home publica

Orden objetivo:

```text
Header

Hero

Categorias rapidas

Paquetes destacados

Sedes destacadas

Como funciona

Servicios adicionales

FAQ

CTA final

Footer
```

---

# 18. Flujo de reserva

El proceso debe sentirse progresivo.

Orden conceptual:

```text
1. Sede
2. Salon
3. Fecha
4. Horario
5. Invitados
6. Paquete
7. Tematica
8. Servicios adicionales
9. Resumen
10. Confirmacion
```

El orden definitivo debe respetar las reglas y dependencias de la API existente.

No se debe duplicar logica del backend en JavaScript.

---

# 19. Resumen de reserva

Desktop:

panel lateral.

Mobile:

barra inferior o bloque antes del CTA.

Debe mostrar como minimo:

```text
Sede
Salon
Fecha
Horario
Invitados
Paquete
Servicios
Monto total
Deposito requerido
```

Los montos son obtenidos del backend.

---

# 20. Formularios

Inputs:

```text
altura minima 48px
label visible
radio consistente
feedback junto al campo
```

No utilizar placeholder como sustituto permanente del label.

Estados:

```text
default
focus
error
disabled
```

---

# 21. Login y registro

Desktop:

```text
+---------------------+---------------------+
| Imagen / branding   | Formulario          |
|                     |                     |
|                     | Login / registro    |
+---------------------+---------------------+
```

Mobile:

```text
Logo
Titulo
Formulario
CTA
```

El formulario debe ser la prioridad.

---

# 22. Mi cuenta

Navegacion:

```text
Mi perfil
Mis reservas
Mis pagos
Seguridad
Cerrar sesion
```

El frontend no almacenara passwords ni secretos.

---

# 23. Mis reservas

Cada reserva debe mostrar:

```text
Numero o identificador
Fecha del evento
Sede
Salon
Estado
Monto
Accion Ver detalle
```

Los estados visuales deben reutilizar los estados reales del backend.

---

# 24. Pagos

La interfaz inicial solamente debe representar la funcionalidad existente en FASE 7.6F.

No debe simular todavia integracion completa con Mercado Pago.

Mercado Pago pertenece a su fase posterior.

---

# 25. BackOffice

Layout:

```text
+----------------+----------------------------------------+
| Sidebar        | Topbar                                 |
|                +----------------------------------------+
| Dashboard      |                                        |
| Reservas       | Contenido                              |
| Pagos          |                                        |
| Sede           |                                        |
| Usuarios *     |                                        |
+----------------+----------------------------------------+
```

`Usuarios` solamente para ADMINISTRADOR.

ENCARGADO_LOCAL nunca debe recibir opciones visuales que impliquen alcance global.

El backend sigue siendo responsable de autorizar.

---

# 26. Tablas BackOffice

Deben soportar:

- encabezado claro.
- filtros.
- estado.
- acciones.
- responsive.

En mobile puede utilizarse:

```text
tabla con scroll horizontal
```

o:

```text
representacion en cards
```

segun el tipo de informacion.

---

# 27. Estados vacios

Ejemplos:

```text
Todavia no tienes reservas.
No hay pagos registrados.
No encontramos sedes.
```

Siempre que tenga sentido debe existir una accion sugerida.

Ejemplo:

```text
[ RESERVAR MI FIESTA ]
```

---

# 28. Loading

Para cargas breves:

```text
spinner
```

Para listas visuales:

```text
skeleton cards
```

Nunca debe bloquearse toda la interfaz si solamente se actualiza una seccion.

---

# 29. Errores

HTTP y UI deben diferenciar:

```text
400 entrada invalida
401 sesion requerida
403 acceso prohibido
404 recurso no visible o inexistente
409 conflicto de negocio
500 error inesperado
```

No se deben mostrar stack traces al usuario.

---

# 30. Accesibilidad

Requisitos minimos:

- HTML semantico.
- labels asociados.
- navegacion por teclado.
- `focus-visible`.
- contraste suficiente.
- imagenes con `alt`.
- botones reales para acciones.
- enlaces reales para navegacion.
- estados no comunicados solo mediante color.
- targets tactiles adecuados.

---

# 31. Imagenes

Preferencias:

- formato WebP cuando sea viable.
- dimensiones consistentes.
- `object-fit: cover`.
- lazy loading cuando corresponda.
- evitar assets gigantes.

Las imagenes comerciales de terceros no deben copiarse al repositorio sin autorizacion.

---

# 32. Iconografia

Se elegira una unica familia de iconos.

No mezclar multiples librerias.

La primera version debe preferir SVG o una libreria ligera.

---

# 33. CSS

El frontend tendra una capa propia de estilos.

Estructura objetivo:

```text
wwwroot/css/
    site.css
    tokens.css
    components.css
    utilities.css
```

Puede utilizarse Bootstrap como soporte de grid y componentes basicos.

Bootstrap no define la identidad final.

Los estilos propios de Bembos Events tendran prioridad visual.

---

# 34. JavaScript

JavaScript se utilizara solamente cuando aporte comportamiento real.

Ejemplos:

- menu mobile.
- seleccion progresiva.
- modales.
- feedback.
- componentes interactivos.

No se trasladaran reglas de negocio del backend a JavaScript.

---

# 35. Razor

Las partes repetidas deben convertirse en partials o componentes.

Candidatos:

```text
_Header
_Footer
_ReservationSummary
_PackageCard
_SedeCard
_ThemeCard
_ServiceCard
_StatusBadge
_Alert
_Pagination
```

---

# 36. Layouts

Se preveen dos layouts principales.

```text
_Layout.cshtml
```

Portal publico / cliente.

```text
_BackOfficeLayout.cshtml
```

Administracion.

No deben mezclarse menus administrativos con el portal publico.

---

# 37. Responsive cards

Desktop:

```text
4 columnas cuando exista espacio suficiente.
```

Tablet:

```text
2 o 3 columnas.
```

Mobile:

```text
1 columna.
```

La cantidad exacta dependera del contenido.

---

# 38. Navegacion

La ruta visible debe corresponder al contexto real.

No esconder operaciones no autorizadas como unica medida de seguridad.

El backend Spring Boot siempre valida permisos.

---

# 39. Animaciones

Permitidas:

```text
150ms - 250ms
```

Para:

- hover.
- apertura.
- focus.
- transiciones visuales menores.

Evitar:

- animaciones continuas.
- movimientos excesivos.
- efectos que dificulten lectura.

Debe respetarse:

```css
prefers-reduced-motion
```

---

# 40. Identidad de Bembos Events

El resultado visual debe poder describirse como:

```text
Bembos
+
fiestas
+
reservas digitales
+
experiencia moderna
```

No como:

```text
dashboard Bootstrap generico
```

ni como:

```text
copia HTML del ecommerce de Bembos
```

---

# 41. Definition of Done visual

Una pantalla frontend no esta terminada hasta comprobar:

- desktop.
- tablet.
- mobile.
- loading.
- empty state cuando aplique.
- error state.
- autorizacion.
- navegacion por teclado.
- contraste.
- integracion real con API.
- sin acceso directo a SQL Server.
- sin reglas de negocio duplicadas.
- sin secretos.
- estilos coherentes con este Design System.

---

# 42. Orden inicial de FASE 8

```text
8.0.1 Auditoria frontend
8.0.2 Design System
8.0.3 Crear solution y proyecto ASP.NET Core MVC
8.0.4 Infraestructura HttpClient hacia Spring Boot
8.0.5 Layout publico
8.0.6 Home
8.0.7 Autenticacion
```

Las siguientes subfases se detallaran a medida que avance la implementacion.

---

# 43. Regla arquitectonica definitiva

```text
Browser
   |
   v
ASP.NET Core MVC
   |
   | HTTP REST
   v
Spring Boot
   |
   v
Service
   |
   v
Repository
   |
   v
SQL Server
```

ASP.NET Core MVC nunca accede directamente a SQL Server.

Spring Boot sigue siendo la autoridad de seguridad y negocio.
