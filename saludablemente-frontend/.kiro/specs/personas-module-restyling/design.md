# Design Document — personas-module-restyling

## Overview

Este documento describe los cambios de CSS necesarios para alinear el módulo `persona` con el sistema de diseño de Saludablemente. El trabajo es exclusivamente cosmético: se modifican **tres archivos `.css`** y **ningún archivo `.html` ni `.ts`**. El módulo de referencia canónica es `noticias/noticia`, que ya cumple con todos los patrones visuales.

### Alcance

| Archivo | Tipo de cambio |
|---|---|
| `persona-list.css` | Corrección de 4 propiedades en `.modulo-icono` |
| `persona-form.css` | Adición de 5 propiedades en `form .card h3` |
| `persona-detail.css` | Verificación y documentación (sin cambios) |

---

## Architecture

El módulo `persona` es un conjunto de tres componentes Angular standalone que comparten el sistema de tokens CSS global definido en `styles.css`. No existe lógica nueva ni cambios de estructura.

```
styles.css          ← tokens globales: --sal-*, --tui-*
    │
    ├── persona-list.css     ← .modulo-icono (CORREGIR)
    ├── persona-form.css     ← form .card h3 (CORREGIR)
    └── persona-detail.css   ← .section-title h3, .danger-zone h3 (VERIFICAR)
```

El patrón visual de referencia fluye de `noticia-list.css` hacia `persona-list.css`. El estado final de `.modulo-icono` en ambos archivos debe ser idéntico.

---

## Components and Interfaces

### Componente 1: `persona-list`

Muestra la lista paginada de personas con filtros. La cabecera contiene un ícono de módulo (`.modulo-icono`) seguido del título y subtítulo.

**Selector CSS afectado:** `.modulo-icono`

Estado actual (incorrecto):
```css
.modulo-icono {
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 16px;                  /* literal — debe ser token */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: var(--sal-primary);            /* token incorrecto */
  background: rgb(0 161 255 / 0.12);   /* valor rgb literal */
  flex-shrink: 0;
  /* falta: border-left */
}
```

Estado objetivo (idéntico a `noticia-list.css`):
```css
.modulo-icono {
  width: 2.75rem;
  height: 2.75rem;
  border-radius: var(--tui-radius-l);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: var(--sal-accent);
  background: var(--tui-background-neutral-1);
  border-left: 3px solid var(--sal-accent);
  flex-shrink: 0;
}
```

### Componente 2: `persona-form`

Formulario multi-sección. Cada sección está en una `.card` con un `<h3>` como encabezado. Actualmente esos `<h3>` carecen de la jerarquía visual (peso tipográfico, separador inferior) que el sistema de diseño define.

**Selector CSS afectado:** `form .card h3`

Estado actual (incompleto):
```css
form .card h3 {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  margin-top: 0;
  color: var(--tui-text-primary);
}
```

Estado objetivo (con jerarquía visual completa):
```css
form .card h3 {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  margin-top: 0;
  margin-bottom: 1rem;
  color: var(--tui-text-primary);
  font-size: 0.95rem;
  font-weight: 600;
  border-bottom: 1px solid var(--sal-line);
  padding-bottom: 0.6rem;
}
```

### Componente 3: `persona-detail`

Vista de detalle/perfil. Los selectores relevantes **ya están correctos** — este componente solo necesita verificación documental.

**Selector `.section-title h3` — sin cambios requeridos:**
```css
.section-title h3 {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--sal-ink);   /* token correcto */
}
```

**Selector `.danger-zone h3` — sin cambios requeridos:**
```css
.danger-zone h3 {
  color: var(--sal-danger);   /* token correcto */
}
```

Ambos selectores ya usan tokens `--sal-*`. No se introduce ninguna modificación.

---

## Data Models

No aplica — este restyling no involucra modelos de datos ni lógica TypeScript. Los únicos "datos" relevantes son los tokens CSS, definidos en `styles.css`:

| Token | Valor light | Valor dark | Uso |
|---|---|---|---|
| `--sal-accent` | `#b45309` | `#e8b64c` | Color del ícono y borde izquierdo del módulo |
| `--tui-background-neutral-1` | (Taiga) | (Taiga) | Fondo del ícono de módulo |
| `--tui-radius-l` | (Taiga) | (Taiga) | Border-radius del ícono de módulo |
| `--sal-line` | `#e4ebf0` | `#22314d` | Separador inferior de `<h3>` en formulario |
| `--sal-ink` | `#111c2d` | `#e8edf5` | Color de texto principal en detail |
| `--sal-danger` | `#e57373` | `#e57373` | Color de la zona peligrosa |

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Este feature es de CSS puro, sin funciones con espacio de inputs variable. La mayoría de los criterios de aceptación son afirmaciones estáticas sobre el contenido de archivos (presencia/ausencia de cadenas). El análisis de prework identificó **dos propiedades universales** genuinas que valen la pena formalizar: la ausencia de valores literales de color en cualquier archivo CSS del módulo.

> **Nota de aplicabilidad**: Las propiedades que siguen son verificables mediante grep/lint de archivos estáticos. No se aplica property-based testing con generadores de inputs aleatorios porque el dominio no es un espacio de entradas de función sino el contenido de archivos CSS fijos. La herramienta adecuada es un linter o script de verificación.

### Property 1: Ausencia de valores RGB literales en persona-list.css

*Para cualquier* línea del archivo `persona-list.css` que no sea un comentario, dicha línea NO deberá contener la expresión `rgb(` como valor de color.

**Validates: Requirements 4.1, 4.4**

### Property 2: Ausencia de valores hex directos en todos los CSS del módulo persona

*Para cualquier* archivo CSS del módulo persona (`persona-list.css`, `persona-form.css`, `persona-detail.css`), ninguna línea que no sea comentario deberá contener un valor de color hexadecimal directo (patrón `#[0-9a-fA-F]{3,8}`).

**Validates: Requirements 5.5**

---

## Error Handling

No aplica en el sentido de excepciones de runtime. Los únicos "errores" posibles son:

| Situación | Consecuencia | Mitigación |
|---|---|---|
| Usar un token inexistente | El estilo se queda sin valor (falla silenciosa en CSS) | Verificar que todos los tokens usados están definidos en `styles.css` antes de hacer el commit |
| Sobreescribir una propiedad necesaria | Regresión visual | La tarea 5 del plan verifica que no haya clases CSS referenciadas sin definición |
| Modificar un archivo `.ts` por error | Riesgo de romper lógica | Restricción explícita en el plan: solo se tocan los 3 archivos `.css` |

---

## Testing Strategy

Este feature **no es candidato para property-based testing con generadores de inputs aleatorios** (no hay funciones puras con espacio de inputs variable). En su lugar, la estrategia se basa en verificaciones estáticas de archivos.

### Verificaciones estáticas (grep/lint)

Estas verificaciones se ejecutan manualmente o como parte de un script de CI antes del merge.

**V1 — `.modulo-icono` tiene los tokens correctos** (valida Requisito 1 completo):
```bash
grep -n "color: var(--sal-accent)" persona-list.css         # debe aparecer
grep -n "background: var(--tui-background-neutral-1)" persona-list.css  # debe aparecer
grep -n "border-left: 3px solid var(--sal-accent)" persona-list.css     # debe aparecer
grep -n "border-radius: var(--tui-radius-l)" persona-list.css           # debe aparecer
```

**V2 — Ausencia de valores incorrectos en `.modulo-icono`** (valida Requisito 4):
```bash
grep -n "rgb(0 161 255" persona-list.css      # NO debe aparecer
grep -n "var(--sal-primary)" persona-list.css  # NO debe aparecer en .modulo-icono
grep -n "border-radius: 16px" persona-list.css # NO debe aparecer
```

**V3 — `form .card h3` tiene la jerarquía visual completa** (valida Requisito 2):
```bash
grep -n "font-weight: 600" persona-form.css          # debe aparecer
grep -n "font-size: 0.95rem" persona-form.css        # debe aparecer
grep -n "border-bottom: 1px solid var(--sal-line)" persona-form.css  # debe aparecer
grep -n "padding-bottom: 0.6rem" persona-form.css    # debe aparecer
grep -n "margin-bottom: 1rem" persona-form.css       # debe aparecer
```

**V4 — `persona-detail.css` mantiene tokens correctos** (valida Requisito 3):
```bash
grep -n "color: var(--sal-ink)" persona-detail.css     # debe aparecer en .section-title h3
grep -n "color: var(--sal-danger)" persona-detail.css  # debe aparecer en .danger-zone h3
```

**V5 — Propiedad 2: Sin hex literales en ningún CSS del módulo** (valida Requisito 5.5):
```bash
grep -Pn "(?<!\/\/.*)#[0-9a-fA-F]{3,8}\b" persona-list.css persona-form.css persona-detail.css
# No debe retornar ninguna línea
```

### Verificación visual

Tras aplicar los cambios, abrir la aplicación con tema claro y oscuro y confirmar:
- El ícono de módulo en `persona-list` muestra el acento correcto (amber en oscuro, brown en claro) con borde izquierdo.
- Los encabezados de sección en `persona-form` muestran separador inferior visible.
- El perfil en `persona-detail` mantiene sus estilos de sección y zona peligrosa sin regresión.
