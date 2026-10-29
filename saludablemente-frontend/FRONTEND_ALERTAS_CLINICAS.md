# Frontend de etiquetas y asociaciones clínicas

## Acceso

Desde el módulo **Alertas clínicas** se añadieron accesos a Etiquetas clínicas, Parámetros clínicos y Reporte de etiquetas. Las rutas conservan el RoleGuard de Administrador que ya utiliza esta sección.

| Ruta | Función |
|---|---|
| `/alertaclinica/etiquetas` | Catálogo con búsqueda, estado y paginación real |
| `/alertaclinica/etiquetas/nueva` | Cabecera de etiqueta y FormArray de parámetros |
| `/alertaclinica/etiquetas/:id/editar` | Edición con revisión y código inmutable |
| `/alertaclinica/etiquetas/:id` | Datos de etiqueta y cards independientes de parámetros y alertas |
| `/alertaclinica/parametros` | Consulta de parámetros activos o inactivos |
| `/alertaclinica/parametros/:id` | Card de etiquetas del parámetro |
| `/alertaclinica/reportes/etiquetas` | Filtros, indicadores y detalle expandible |
| `/alertaclinica/alertas/:id` | Nueva card de etiquetas dentro del detalle existente |

## Operación

Cada card carga su revisión del backend, permite agregar varias filas y retirar asociaciones manuales, muestra los cambios pendientes y confirma el conjunto completo antes de un único PUT. No persiste al agregar o quitar una fila. Las asociaciones MOTOR permanecen protegidas y se incluyen en el conjunto enviado.

Las cuatro direcciones usan el mismo componente. Los errores de guardado conservan el borrador; un conflicto 409 solicita recargar explícitamente. Cancelar una confirmación no envía peticiones. Reagregar una asociación original recupera sus datos históricos, aunque el catálogo haya cambiado.

El formulario de etiquetas envía `detalles: [{"idParametro": 1}]`, junto con los campos de cabecera y la revisión al editar. Su vista previa y resumen se actualizan con signals/computed. Se permiten etiquetas sin parámetros para uso manual.

Al salir de una pantalla con cambios se solicita confirmación. También se protege el cierre de pestaña y se impide navegar durante un guardado. Atender o anular una alerta requiere guardar o cancelar primero la edición de sus etiquetas.

## Diseño y accesibilidad

- Taiga UI y variables existentes `--sal-*` / `--tui-*`.
- Cards redondeadas, listas de detalle y estados identificados con texto.
- Resumen lateral en escritorio; distribución en una columna en móvil.
- Navegación por teclado, foco visible, labels, estados de carga y mensajes accesibles.
- Tema claro y oscuro. Los colores personalizados se limitan a marcadores y no determinan la legibilidad del texto.
- Revisión visual a 390 px: formulario, detalle y reporte sin desbordamiento horizontal.

## Cambios principales

Los archivos nuevos están en `src/app/features/admin/alertaclinica/etiquetas/`:

| Archivo | Responsabilidad |
|---|---|
| `etiqueta.model.ts` | Tipos de etiquetas, parámetros, tarjetas y reportes |
| `etiqueta-service.ts` | HTTP, contratos compuestos y adaptación de las cuatro direcciones |
| `asociaciones-draft.ts` | FormArray, diferencias, duplicados, restauración y protección MOTOR |
| `asociaciones-card.ts/html` | Selección, búsqueda, resumen, confirmación y guardado transaccional |
| `etiqueta-list.ts/html` | Catálogo, filtros, paginación y cambio de estado |
| `etiqueta-form.ts/html` | Crear/editar etiqueta con parámetros |
| `etiqueta-detail.ts/html` | Cabecera y cards de parámetros y alertas |
| `parametro-list.ts`, `parametro-detail.ts` | Consulta y edición de la relación inversa |
| `etiqueta-reporte.ts/html` | Reporte con filtros, KPIs y alertas expandibles |
| `etiqueta-dashboard-card.ts` | Top histórico de asignaciones en el dashboard existente |
| `clinica-nav.ts` | Navegación entre las secciones del módulo |
| `clinica.css` | Diseño compartido, responsive y compatible con los temas actuales |
| `cambios-pendientes.ts` | Guard de salida y protección de cierre de pestaña |
| `asociaciones.spec.ts` | Pruebas del borrador, contratos HTTP y comportamiento de la card |

Archivos existentes modificados:

- `app.routes.ts`: rutas y guards de cambios pendientes.
- `features/admin/home/admin-modulos.ts`: accesos dentro del módulo.
- `alerta/alerta-clinica.model.ts` y `alerta-clinica-service.ts`: etiquetas y filtro HTTP.
- `alerta/alerta-clinica-list.ts/html/css`: filtro de etiquetas, distintivos y navegación.
- `alerta/alerta-clinica-detail.ts/html`: card de asociaciones y coordinación con atender/anular.
- `dashboard/dashboard.ts/html`: navegación y card de asignaciones históricas.

## Verificación

```text
node node_modules/@angular/cli/bin/ng.js build --configuration production
node node_modules/@angular/cli/bin/ng.js test --watch=false --include="**/etiquetas/*.spec.ts"
node node_modules/@angular/cli/bin/ng.js test --watch=false --include="**/alertaclinica/alerta/*.spec.ts" --include="**/alertaclinica/dashboard/*.spec.ts"
```

La compilación de producción se completó. Las 11 pruebas nuevas verifican duplicados, protección MOTOR, conservación histórica, cancelación, paginación del catálogo, una petición por operación, revisión del servidor y preservación del borrador ante 409.

También pasaron las 14 pruebas existentes del listado, detalle y dashboard de Alertas Clínicas: **25 pruebas aprobadas en total**, sin fallos.

Se revisaron visualmente el catálogo, la creación y confirmación de etiqueta, los detalles, la asignación de alertas, la protección al salir y el reporte, usando datos sintéticos. Se comprobó la expansión del reporte con teclado y no se observaron errores de consola en ese recorrido.

Queda pendiente una comprobación de integración con una sesión autenticada y el backend/Oracle reales; la revisión visual no escribe datos clínicos reales. Permanecen las advertencias generales del proyecto sobre imports no utilizados, el tamaño inicial y estilos de otras pantallas. Los estilos nuevos están dentro del umbral de 6 kB.

## Evidencia visual y vista QA

- `docs/alertas-ui/etiquetas-escritorio.jpg`
- `docs/alertas-ui/formulario-movil.jpg`
- `docs/alertas-ui/reporte-escritorio.jpg`

`tools/alertas-ui-preview/` contiene un bootstrap independiente con datos de prueba en memoria. No se importa desde `src/main.ts`, no modifica la autenticación de la aplicación y no conecta con el backend.

Para reconstruirlo y servirlo localmente:

```text
node node_modules/@angular/cli/bin/ng.js build --configuration development --browser tools/alertas-ui-preview/main.ts --ts-config tools/alertas-ui-preview/tsconfig.json --output-path dist/alertas-ui-preview
node tools/alertas-ui-preview/server.cjs
```

Vista QA: `http://127.0.0.1:4175`, solamente loopback y datos sintéticos. No usarla como aplicación productiva.
