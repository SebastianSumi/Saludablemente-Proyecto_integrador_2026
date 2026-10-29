# Implementation Plan — personas-module-restyling

## Tasks

- [x] 1. Corregir `.modulo-icono` en `persona-list.css`
  - Reemplazar `color: var(--sal-primary)` por `color: var(--sal-accent)`
  - Reemplazar `background: rgb(0 161 255 / 0.12)` por `background: var(--tui-background-neutral-1)`
  - Reemplazar `border-radius: 16px` por `border-radius: var(--tui-radius-l)`
  - Añadir `border-left: 3px solid var(--sal-accent)` al selector
  - Conservar sin cambios `width`, `height`, `display`, `align-items`, `justify-content`, `font-size`, `flex-shrink`
  - **Archivos:** `persona-list.css`
  - **Requisitos:** 1.1, 1.2, 1.3, 1.4, 1.5

- [x] 2. Mejorar `form .card h3` en `persona-form.css`
  - Añadir `font-weight: 600` a la regla `form .card h3`
  - Añadir `font-size: 0.95rem` a la regla `form .card h3`
  - Añadir `border-bottom: 1px solid var(--sal-line)` a la regla `form .card h3`
  - Añadir `padding-bottom: 0.6rem` a la regla `form .card h3`
  - Añadir `margin-bottom: 1rem` a la regla `form .card h3`
  - Conservar sin cambios `display`, `align-items`, `gap`, `margin-top`, `color`
  - **Archivos:** `persona-form.css`
  - **Requisitos:** 2.1, 2.2, 2.3, 2.4, 2.5, 2.6

- [x] 3. Verificar y documentar `persona-detail.css`
  - Confirmar que `.section-title h3` contiene `font-size: 0.95rem`, `font-weight: 700`, `color: var(--sal-ink)`
  - Confirmar que `.danger-zone h3` contiene `color: var(--sal-danger)`
  - Confirmar que `.section-title` conserva `display: flex`, `align-items: center`, `justify-content: space-between`, `gap: 0.75rem`, `margin: 0 0 1rem`, `flex-wrap: wrap`
  - No aplicar ningún cambio si los valores ya son correctos
  - **Archivos:** `persona-detail.css` (solo lectura)
  - **Requisitos:** 3.1, 3.2, 3.3, 3.4, 3.5

- [x] 4. Verificar ausencia de valores literales de color en los tres CSS del módulo
  - Confirmar que `persona-list.css` no contiene `rgb(0 161 255`, `var(--sal-primary)` en `.modulo-icono`, ni `border-radius: 16px`
  - Confirmar que ninguno de los tres archivos CSS contiene un valor hexadecimal directo (`#[0-9a-fA-F]{3,8}`) fuera de comentarios
  - Confirmar que `persona-list.css` no contiene un bloque `body[tuiTheme='dark']` de override manual
  - **Archivos:** `persona-list.css`, `persona-form.css`, `persona-detail.css`
  - **Requisitos:** 4.1, 4.2, 4.3, 4.4, 5.5

- [x] 5. Verificar que los templates HTML no necesitan cambios
  - Leer `persona-list.html`, `persona-form.html` y `persona-detail.html`
  - Confirmar que todas las clases CSS referenciadas en los templates están definidas en `styles.css` o en el `.css` del propio componente
  - Confirmar que no se modificó ningún binding Angular, atributo `aria-*`, clase `sr-only`, ni texto alternativo
  - Confirmar que no se modificó ningún archivo `.ts` o `.spec.ts`
  - **Archivos:** `persona-list.html`, `persona-form.html`, `persona-detail.html` (solo lectura)
  - **Requisitos:** 5.1, 5.2, 5.3, 5.4
