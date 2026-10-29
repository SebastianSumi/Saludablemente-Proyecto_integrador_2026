# Requirements Document

## Introduction

El módulo `persona` de la aplicación Angular/Taiga UI requiere una unificación visual con el resto del sistema de diseño. Los archivos `persona-list`, `persona-form` y `persona-detail` usan estilos ad-hoc que difieren de los patrones establecidos por el módulo `noticia` (referencia canónica) y las clases globales de `styles.css`. El objetivo es modificar exclusivamente los archivos `.html` y `.css` del módulo para alinearlos con los tokens `--sal-*` / `--tui-*` y los patrones visuales vigentes, sin alterar ningún archivo `.ts`.

## Glosario

- **Sistema_Diseño**: El conjunto de tokens CSS (`--sal-*`, `--tui-*`), clases globales (`.card`, `.pill`, `.filters`, `.table-wrapper`, etc.) y convenciones visuales definidos en `styles.css` y materializados en el módulo `noticia`.
- **Modulo_Icono**: El elemento `<div class="modulo-icono">` que contiene el ícono decorativo en cabeceras de lista y formulario.
- **Accent_Token**: La variable `--sal-accent` (#b45309 en light, #e8b64c en dark), color primario de acento del sistema.
- **Persona_List**: El componente de lista de personas (`persona-list.html` + `persona-list.css`).
- **Persona_Form**: El componente de formulario multi-sección (`persona-form.html` + `persona-form.css`).
- **Persona_Detail**: El componente de detalle/perfil de persona (`persona-detail.html` + `persona-detail.css`).
- **Section_Title**: El bloque encabezado de sección compuesto por `<div class="section-title"><h3>…</h3></div>` dentro de una `.card`.

---

## Requisitos

### Requisito 1: Corrección del Modulo_Icono en Persona_List

**User Story:** Como desarrollador frontend, quiero que el Modulo_Icono de Persona_List use los mismos tokens y reglas visuales que el módulo noticia, para que el encabezado de la lista sea visualmente consistente con el resto de la aplicación.

#### Criterios de aceptación

1. THE Persona_List SHALL aplicar `color: var(--sal-accent)` al selector `.modulo-icono`, reemplazando el valor actual `color: var(--sal-primary)`.
2. THE Persona_List SHALL aplicar `background: var(--tui-background-neutral-1)` al selector `.modulo-icono`, reemplazando el valor actual `background: rgb(0 161 255 / 0.12)`.
3. THE Persona_List SHALL aplicar `border-left: 3px solid var(--sal-accent)` al selector `.modulo-icono`, que actualmente no existe en el selector.
4. THE Persona_List SHALL aplicar `border-radius: var(--tui-radius-l)` al selector `.modulo-icono`, reemplazando el valor literal actual `border-radius: 16px`.
5. THE Persona_List SHALL conservar sin cambios las demás propiedades del selector `.modulo-icono` (`width`, `height`, `display`, `align-items`, `justify-content`, `font-size`, `flex-shrink`).

---

### Requisito 2: Consistencia visual de los encabezados de sección `<h3>` en Persona_Form

**User Story:** Como desarrollador frontend, quiero que los encabezados de sección `<h3>` dentro de `persona-form` tengan un tratamiento visual explícito y consistente con el sistema de diseño, para que el formulario multi-sección muestre jerarquía visual clara.

#### Criterios de aceptación

1. THE Persona_Form SHALL aplicar `font-weight: 600` a la regla CSS de `form .card h3`.
2. THE Persona_Form SHALL aplicar `font-size: 0.95rem` a la regla CSS de `form .card h3`.
3. THE Persona_Form SHALL aplicar `border-bottom: 1px solid var(--sal-line)` a la regla CSS de `form .card h3`, para separar visualmente el título del contenido de la sección.
4. THE Persona_Form SHALL aplicar `padding-bottom: 0.6rem` a la regla CSS de `form .card h3` como complemento del borde inferior.
5. THE Persona_Form SHALL aplicar `margin-bottom: 1rem` a la regla CSS de `form .card h3` para mantener separación respecto a los campos.
6. THE Persona_Form SHALL conservar sin cambios las propiedades `display: flex`, `align-items: center`, `gap: 0.45rem`, `margin-top: 0` y `color: var(--tui-text-primary)` ya existentes en `form .card h3`.

---

### Requisito 3: Consistencia visual de los encabezados de sección `<h3>` en Persona_Detail

**User Story:** Como desarrollador frontend, quiero que los `<h3>` dentro de `.section-title` en `persona-detail` usen los mismos tokens y escala tipográfica que el sistema de diseño, para que los títulos de cada tarjeta sean coherentes con los de `persona-form` y el resto de la aplicación.

#### Criterios de aceptación

1. THE Persona_Detail SHALL aplicar `font-size: 0.95rem` al selector `.section-title h3`, alineándolo con la escala usada en `persona-form` y `persona-detail` ya vigente.
2. THE Persona_Detail SHALL aplicar `font-weight: 700` al selector `.section-title h3` (el valor actual `font-weight: 700` ya existe; este requisito documenta que DEBE mantenerse).
3. THE Persona_Detail SHALL aplicar `color: var(--sal-ink)` al selector `.section-title h3` (el valor actual ya existe; DEBE mantenerse sin cambio a colores literales).
4. WHEN la sección sea la zona peligrosa (`.danger-zone`), THE Persona_Detail SHALL aplicar `color: var(--sal-danger)` al selector `.danger-zone h3`, sobrescribiendo el color base heredado.
5. THE Persona_Detail SHALL conservar el selector `.section-title` con `display: flex`, `align-items: center`, `justify-content: space-between`, `gap: 0.75rem`, `margin: 0 0 1rem` y `flex-wrap: wrap` sin alteraciones.

---

### Requisito 4: Eliminación de valores literales de color y dimensión en Persona_List

**User Story:** Como desarrollador frontend, quiero que `persona-list.css` no contenga valores de color o dimensión codificados literalmente (hex, rgb), para que el componente respete el sistema de tokens y se adapte correctamente al modo oscuro.

#### Criterios de aceptación

1. THE Persona_List SHALL NO contener la expresión `rgb(0 161 255 / 0.12)` en ningún selector de `persona-list.css` tras el cambio.
2. THE Persona_List SHALL NO contener la expresión `var(--sal-primary)` como valor de `color` en el selector `.modulo-icono` tras el cambio.
3. THE Persona_List SHALL NO contener el valor literal `16px` como `border-radius` en el selector `.modulo-icono` tras el cambio.
4. WHEN el tema activo sea oscuro (`body[tuiTheme='dark']`), THE Persona_List SHALL renderizar el Modulo_Icono con el color y fondo correctos a través de los tokens `--sal-accent` y `--tui-background-neutral-1`, sin requerir overrides adicionales en `persona-list.css`.

---

### Requisito 5: Preservación de lógica, accesibilidad y estructura HTML

**User Story:** Como desarrollador frontend, quiero que ningún cambio de estilización altere la funcionalidad, la accesibilidad ni la estructura de los templates, para que el restyling sea seguro y no rompa nada.

#### Criterios de aceptación

1. THE Sistema_Diseño SHALL NO modificar ningún archivo con extensión `.ts` o `.spec.ts` del módulo persona.
2. THE Sistema_Diseño SHALL conservar todos los atributos `aria-*`, roles, clases `sr-only` y textos alternativos presentes en los templates HTML.
3. THE Sistema_Diseño SHALL conservar todos los bindings de Angular (`[formGroup]`, `[formControl]`, `(ngSubmit)`, `[routerLink]`, señales, etc.) sin alteraciones.
4. IF se añade o modifica una clase CSS en un template HTML, THEN THE Sistema_Diseño SHALL verificar que esa clase esté definida en `styles.css` o en el archivo `.css` del propio componente.
5. THE Sistema_Diseño SHALL NO introducir valores de color hexadecimales directos en los archivos `.css` del módulo persona; todos los colores DEBEN expresarse mediante `var(--sal-*)` o `var(--tui-*)`.
