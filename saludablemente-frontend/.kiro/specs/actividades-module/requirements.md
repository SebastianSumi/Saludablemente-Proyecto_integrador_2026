# Requirements Document — Módulo Actividades (Frontend)

## Introduction

El módulo `actividades` de la aplicación Angular/Taiga UI permite a los administradores gestionar el ciclo de vida completo de las actividades institucionales: desde su programación y configuración hasta su ejecución, control de asistencias, inscripciones y reporte de resultados. El módulo cubre cuatro vistas principales: **Dashboard**, **Lista**, **Formulario crear/editar** y **Detalle** (con sub-secciones de configuración, inscripciones, operación y QR). El estilo visual debe seguir los patrones del módulo `alertaclinica` (page-banner, pills, tiles, flush tables, KPI cards) y `personas` (hero card con banner, secciones con `section-title`), usando exclusivamente tokens `--sal-*` / `--tui-*`, componentes Taiga UI y standalone components con Angular Signals.

---

## Glossary

- **Actividad**: Entidad principal del módulo. Representa un evento institucional con fecha, hora, lugar, coordinador y un ciclo de vida de estados.
- **Estado**: Valor del campo `estado` de una Actividad. Los valores válidos son `PROGRAMADA`, `EN_CURSO`, `FINALIZADA` y `CANCELADA`.
- **Ciclo_De_Vida**: La secuencia de transiciones de estado válidas: `PROGRAMADA → EN_CURSO → FINALIZADA` y `PROGRAMADA → CANCELADA`. No se permiten otras transiciones.
- **Actividad_List**: El componente de tabla con filtros (`actividad-list.html` + `actividad-list.css`).
- **Actividad_Form**: El componente de formulario crear/editar (`actividad-form.html` + `actividad-form.css`).
- **Actividad_Detail**: El componente de detalle tabbed (`actividad-detail.html` + `actividad-detail.css`).
- **Actividad_Dashboard**: El componente de dashboard KPI + tabla reciente (`actividad-dashboard.html` + `actividad-dashboard.css`).
- **Actividad_Reporte**: El componente de reporte por período (`actividad-reporte.html` + `actividad-reporte.css`).
- **Page_Banner**: Bloque de cabecera de página con ícono tile, título, subtítulo y acciones. Usa las clases `.page-banner.card` + `.page-banner-body` del sistema de diseño, tal como en `alertaclinica`.
- **KPI_Card**: Tarjeta de métrica individual con ícono tile, pill de categoría, valor numérico grande y etiqueta descriptiva. Usa las clases `.kpi-card.card` del sistema de diseño.
- **Flush_Table**: Tabla dentro de una `.card.flush` donde la tabla toca los bordes del card sin padding lateral.
- **Filtros_Bar**: Barra de filtros con campos ComboBox/Input inline, clase `.filtros-card`, inspirada en `alerta-clinica-list`.
- **Hero_Card**: Tarjeta con banner de gradiente decorativo, avatar/ícono grande, nombre prominente, pills de estado y acciones. Clase `.hero-card.card`.
- **Section_Title**: Bloque de encabezado de sección con tile de ícono, `<h3>` y descripción. Clase `.section-title` dentro de `.card`.
- **Pill**: Badge de estado inline redondeado. Clases `.pill .pill-soft-{color}` del sistema de diseño.
- **Tile**: Ícono cuadrado con borde redondeado o circular. Clases `.tile .tile-rounded .tile-soft-{color}` del sistema de diseño.
- **Token_CSS**: Variable CSS del sistema. Los prefijos válidos son `--sal-*` (colores propios del sistema) y `--tui-*` (variables de Taiga UI). No se permiten valores hex directos fuera de `:root`.
- **AgendaService**: Servicio Angular que consume `/api/v1/agenda`.
- **OperacionService**: Servicio Angular que consume `/api/v1/operacion`.
- **InscripcionService**: Servicio Angular que consume `/api/v1/actividades/{id}/inscripciones`.
- **ConfiguracionActividadService**: Servicio Angular que consume `/api/v1/configuracion`.
- **ReporteActividadService**: Servicio Angular que consume `/api/v1/reportes`.

---

## Requirements

### Requirement 1: Dashboard — KPIs del módulo

**User Story:** As an administrator, I want to see a summary dashboard with key metrics and recent activities, so that I can quickly understand the current state of the activities program.

#### Acceptance Criteria

1. THE Actividad_Dashboard SHALL display a Page_Banner with the icon `@tui.layout-dashboard`, title "Dashboard de Actividades" and a subtitle showing the current date range used for today's query.
2. WHEN the Actividad_Dashboard loads, THE AgendaService SHALL be called with `GET /api/v1/agenda/actividades` to obtain the full list and `GET /api/v1/agenda/hoy` to obtain today's activities.
3. THE Actividad_Dashboard SHALL display a KPI_Card for "Total actividades" showing the count of all activities, using `tile-soft-primary` color and icon `@tui.calendar`.
4. THE Actividad_Dashboard SHALL display a KPI_Card for "Programadas" showing the count of activities with `estado === 'PROGRAMADA'`, using `tile-soft-warning` color and icon `@tui.clock`.
5. THE Actividad_Dashboard SHALL display a KPI_Card for "En curso" showing the count of activities with `estado === 'EN_CURSO'`, using `tile-soft-success` color and icon `@tui.activity`.
6. THE Actividad_Dashboard SHALL display a KPI_Card for "Finalizadas" showing the count of activities with `estado === 'FINALIZADA'`, using `tile-soft-secondary` color and icon `@tui.circle-check`.
7. THE Actividad_Dashboard SHALL display a KPI_Card for "Canceladas" showing the count of activities with `estado === 'CANCELADA'`, using `tile-soft-error` color and icon `@tui.x-circle`.
8. THE Actividad_Dashboard SHALL display a KPI_Card for "Actividades hoy" showing the count from the `GET /hoy` response, using `tile-soft-violet` color and icon `@tui.sun`.
9. THE Actividad_Dashboard SHALL display a Flush_Table with the 10 most recently modified activities, with columns: Nombre, Fecha, Horario, Lugar, Coordinador, Estado, Acciones.
10. WHEN an activity row is shown in the Flush_Table, THE Actividad_Dashboard SHALL render the Estado column using a Pill styled as: `pill-soft-primary` for PROGRAMADA, `pill-soft-warning` for EN_CURSO, `pill-soft-success` for FINALIZADA, `pill-soft-neutral` for CANCELADA.
11. WHEN the Flush_Table row has an action, THE Actividad_Dashboard SHALL provide a "Ver detalle" link that navigates to `/actividades/{idActividad}`.
12. THE Actividad_Dashboard SHALL provide navigation buttons in the Page_Banner: "Ver todas las actividades" linking to `/actividades` and "Nueva actividad" linking to `/actividades/nueva`.
13. IF the AgendaService returns an error, THEN THE Actividad_Dashboard SHALL display an error card with icon `@tui.triangle-alert` and the error message.
14. WHILE data is loading, THE Actividad_Dashboard SHALL display a loading card with the text "Cargando dashboard de actividades…".
15. THE Actividad_Dashboard SHALL use only Token_CSS values for all color properties; no hex literals are permitted in `actividad-dashboard.css`.

---

### Requirement 2: Lista de actividades — tabla con filtros

**User Story:** As an administrator, I want to browse all activities in a table with filter controls, so that I can quickly find and act on specific activities.

#### Acceptance Criteria

1. THE Actividad_List SHALL display a Page_Banner with icon `@tui.calendar`, title "Agenda de Actividades" and a subtitle showing the count of activities matching the current filters.
2. THE Actividad_List SHALL provide a Filtros_Bar with a ComboBox filter for "Estado" (options: Todos, PROGRAMADA, EN_CURSO, FINALIZADA, CANCELADA), a date input for "Fecha (desde)", and a text input for "Búsqueda" (free text match against `nombre` and `lugar`).
3. WHEN the user selects a filter value, THE Actividad_List SHALL update the displayed rows immediately (client-side filtering, no new HTTP call required for state/text filters).
4. THE Actividad_List SHALL display a "Limpiar filtros" button that resets all filter controls to their default empty state.
5. THE Actividad_List SHALL display a Flush_Table with the following columns: Nombre, Fecha, Horario, Lugar, Coordinador, Estado, Acciones.
6. WHEN an activity row is displayed, THE Actividad_List SHALL show the Estado column using a Pill: `pill-soft-primary` for PROGRAMADA, `pill-soft-warning` for EN_CURSO, `pill-soft-success` for FINALIZADA, `pill-soft-neutral` for CANCELADA.
7. WHEN an activity has `estado === 'PROGRAMADA'`, THE Actividad_List SHALL display in the Acciones column the buttons: "Ver", "Editar", "Iniciar" and "Cancelar".
8. WHEN an activity has `estado === 'EN_CURSO'`, THE Actividad_List SHALL display in the Acciones column the buttons: "Ver" and "Finalizar".
9. WHEN an activity has `estado === 'FINALIZADA'` or `estado === 'CANCELADA'`, THE Actividad_List SHALL display in the Acciones column only the "Ver" button.
10. WHEN the user clicks "Iniciar" on a PROGRAMADA activity, THE Actividad_List SHALL call `OperacionService.iniciar(id)` and, upon success, update the row's estado to EN_CURSO without reloading the page.
11. WHEN the user clicks "Finalizar" on an EN_CURSO activity, THE Actividad_List SHALL call `OperacionService.finalizar(id)` and, upon success, update the row's estado to FINALIZADA without reloading the page.
12. WHEN the user clicks "Cancelar" on a PROGRAMADA activity, THE Actividad_List SHALL open a confirmation modal requesting a motivo (text, required), and upon confirmation call `OperacionService.cancelar(id, motivo)`.
13. WHEN the filtered list is empty, THE Actividad_List SHALL display an empty state row with the text "No hay actividades con esos filtros."
14. THE Actividad_List SHALL provide a "Nueva actividad" primary button in the Page_Banner that navigates to `/actividades/nueva`.
15. IF the AgendaService returns an error, THEN THE Actividad_List SHALL display an error card with icon `@tui.triangle-alert` and the error message.
16. THE Actividad_List SHALL use only Token_CSS values for all color properties; no hex literals are permitted in `actividad-list.css`.

---

### Requirement 3: Formulario crear/editar actividad

**User Story:** As an administrator, I want to create or edit an activity through a multi-section form, so that I can set all required data before scheduling it.

#### Acceptance Criteria

1. THE Actividad_Form SHALL display a Page_Banner with icon `@tui.calendar-plus` (for creation) or `@tui.calendar-check` (for editing), and title "Nueva Actividad Institucional" or "Editar Actividad" accordingly.
2. THE Actividad_Form SHALL organize fields in two cards: "Datos generales de la actividad" (Section_Title with `tile-soft-primary` and icon `@tui.info`) and "Parámetros de asistencia y geocerca" (Section_Title with `tile-soft-warning` and icon `@tui.map-pin`).
3. THE Actividad_Form SHALL include the following required fields in the first card: `nombre` (text, maxlength 100), `lugar` (text, maxlength 100), `fechaActividad` (date), `horaInicio` (time), `horaFin` (time), and `idUsuarioCoordinador` (ComboBox selecting from active users).
4. THE Actividad_Form SHALL include the following optional fields in the first card: `descripcion` (text, maxlength 300) and `periodo` (text, e.g. "2026-I").
5. THE Actividad_Form SHALL include in the second card: `toleranciaGeocercaMetros` (number, min 0), `duracionMinutos` (number, min 0), `requiereCheckInOut` (checkbox), `permiteAutoservicio` (checkbox), and `permiteEscaneoCoordinador` (checkbox).
6. WHEN the user submits the form with one or more required fields empty or invalid, THE Actividad_Form SHALL display inline validation errors below each invalid field using `<tui-error>`.
7. WHEN `horaFin` is equal to or earlier than `horaInicio`, THE Actividad_Form SHALL display an inline validation error "La hora de fin debe ser posterior a la hora de inicio."
8. WHEN the form is in creation mode and the user submits valid data, THE Actividad_Form SHALL call `AgendaService.crear(request)` and, upon success, navigate to `/actividades/{newId}`.
9. WHEN the form is in edit mode and the user submits valid data, THE Actividad_Form SHALL call the appropriate update endpoint and, upon success, navigate to `/actividades/{id}`.
10. WHEN the user clicks "Cancelar", THE Actividad_Form SHALL navigate back to `/actividades` without saving.
11. WHILE the form submission is in progress, THE Actividad_Form SHALL disable the submit button and show a loading spinner on it.
12. IF the AgendaService returns an error on submission, THEN THE Actividad_Form SHALL display an error card at the top of the form.
13. THE Actividad_Form SHALL use only Token_CSS values for all color properties; no hex literals are permitted in `actividad-form.css`.

---

### Requirement 4: Detalle de actividad — Hero Card y pestañas

**User Story:** As an administrator, I want to view all information about an activity in a structured detail page, so that I can manage its configuration, inscriptions, and lifecycle in one place.

#### Acceptance Criteria

1. THE Actividad_Detail SHALL display a Hero_Card at the top with: the activity name as `hero-nombre`, date + time range + place as `hero-meta`, a Pill showing the Estado (styled as per Requirement 2, AC 6), and action buttons contextual to the current state.
2. WHEN an activity has `estado === 'PROGRAMADA'`, THE Actividad_Detail SHALL show in the Hero_Card the following action buttons: "Editar" (links to `/actividades/{id}/editar`), "Iniciar" (calls `OperacionService.iniciar`), and "Cancelar" (opens motivo modal then calls `OperacionService.cancelar`).
3. WHEN an activity has `estado === 'EN_CURSO'`, THE Actividad_Detail SHALL show in the Hero_Card the following action buttons: "Finalizar" (calls `OperacionService.finalizar`) and "Reprogramar" (navigates to reprogramming section).
4. WHEN an activity has `estado === 'FINALIZADA'` or `estado === 'CANCELADA'`, THE Actividad_Detail SHALL show in the Hero_Card only a "Volver a lista" button that navigates to `/actividades`.
5. THE Actividad_Detail SHALL organize its content in four tab sections using `<tui-tabs>`: "Información", "Inscripciones", "Configuración" and "Operación / QR".
6. THE Actividad_Detail "Información" tab SHALL display two cards: "Datos generales" (detail-grid with all Actividad fields) and "Operaciones de reprogramación" (reprogramar form only visible when estado === 'PROGRAMADA').
7. THE Actividad_Detail "Inscripciones" tab SHALL display a Section_Title with icon `@tui.users`, a counter badge showing total inscritos from `InscripcionService.contar`, a button "Inscribir a todos" (calls `InscripcionService.inscribirTodos`), a ComboBox to add individual persons (calls `InscripcionService.inscribirPersonas`), and a Flush_Table listing current inscribed persons with a "Quitar" action per row.
8. THE Actividad_Detail "Configuración" tab SHALL display two cards: "Público objetivo (equipos)" (checkboxes for team selection with "Guardar público" button calling `ConfiguracionActividadService.asignarPublico`) and "Materiales y puntaje" (textarea for materiales + number input for puntaje with separate save buttons).
9. WHEN the activity `estado === 'EN_CURSO'`, THE Actividad_Detail "Operación / QR" tab SHALL display the current QR token (hash, dates) and a "Rotar QR" button that calls `OperacionService.rotarTokenQr`.
10. WHEN the activity `estado !== 'EN_CURSO'`, THE Actividad_Detail "Operación / QR" tab SHALL display an informational message "El QR operativo solo está disponible cuando la actividad está EN CURSO."
11. THE Actividad_Detail "Operación / QR" tab SHALL also display the ReporteActividad section with: puntajeSalud, coordinador, materiales list and public objetivo pills.
12. WHEN the user clicks "Transferir coordinador" in the Operación/QR tab (available for PROGRAMADA and EN_CURSO), THE Actividad_Detail SHALL open a ComboBox selector and on confirmation call `OperacionService.transferir`.
13. IF the AgendaService returns an error loading the activity, THEN THE Actividad_Detail SHALL display an error card with icon `@tui.triangle-alert`.
14. WHILE data is loading, THE Actividad_Detail SHALL display a loading card with the text "Cargando actividad…".
15. THE Actividad_Detail SHALL use only Token_CSS values for all color properties; no hex literals are permitted in `actividad-detail.css`.

---

### Requirement 5: Reporte por período

**User Story:** As an administrator, I want to query activities grouped by a date range and period, so that I can analyze program execution across reporting cycles.

#### Acceptance Criteria

1. THE Actividad_Reporte SHALL display a Page_Banner with icon `@tui.chart-column`, title "Reporte de Agenda" and subtitle "Actividades agrupadas por período".
2. THE Actividad_Reporte SHALL display a Filtros_Bar with three fields: `fechaInicio` (date, required), `fechaFin` (date, required) and `periodo` (text, optional, e.g. "2026-I"), plus a "Consultar" submit button.
3. WHEN the user submits the filter form with valid dates, THE Actividad_Reporte SHALL call `ReporteActividadService.porPeriodo(fechaInicio, fechaFin, periodo)`.
4. THE Actividad_Reporte SHALL display a KPI_Card row with: Total, Programadas, En curso, Finalizadas and Canceladas — sourced from the `ReportePeriodo` response fields.
5. WHEN the `ReportePeriodo.actividades` list is not empty, THE Actividad_Reporte SHALL display a Flush_Table with columns: Nombre, Fecha, Horario, Lugar, Estado, Acciones (Ver).
6. WHEN `ReportePeriodo.actividades` is empty or null, THE Actividad_Reporte SHALL display the text "Sin actividades en ese rango."
7. IF the ReporteActividadService returns an error, THEN THE Actividad_Reporte SHALL display an error card with icon `@tui.triangle-alert`.
8. THE Actividad_Reporte SHALL apply the Estado Pill styles consistently: `pill-soft-primary` for PROGRAMADA, `pill-soft-warning` for EN_CURSO, `pill-soft-success` for FINALIZADA, `pill-soft-neutral` for CANCELADA.
9. THE Actividad_Reporte SHALL use only Token_CSS values for all color properties; no hex literals are permitted in `actividad-reporte.css`.

---

### Requirement 6: Ciclo de vida de la Actividad — reglas de negocio

**User Story:** As an administrator, I want the UI to enforce the valid lifecycle transitions of an activity, so that invalid state changes are prevented at the presentation layer.

#### Acceptance Criteria

1. THE Actividad_Detail SHALL only display the "Iniciar" action button WHEN the current `estado === 'PROGRAMADA'`.
2. THE Actividad_Detail SHALL only display the "Finalizar" action button WHEN the current `estado === 'EN_CURSO'`.
3. THE Actividad_Detail SHALL only display the "Cancelar" action button WHEN the current `estado === 'PROGRAMADA'`.
4. THE Actividad_Detail SHALL only display the "Reprogramar" form WHEN the current `estado === 'PROGRAMADA'`.
5. THE Actividad_Detail SHALL only display the QR token and "Rotar QR" button WHEN the current `estado === 'EN_CURSO'`.
6. WHEN the user triggers "Cancelar", THE Actividad_Detail SHALL require a non-empty `motivo` field (min 3 characters) before calling `OperacionService.cancelar`.
7. IF `OperacionService.iniciar` returns an error, THEN THE Actividad_Detail SHALL display an error notification and keep the estado as PROGRAMADA without updating the view.
8. IF `OperacionService.finalizar` returns an error, THEN THE Actividad_Detail SHALL display an error notification and keep the estado as EN_CURSO without updating the view.
9. IF `OperacionService.cancelar` returns an error, THEN THE Actividad_Detail SHALL display an error notification and keep the estado as PROGRAMADA without updating the view.
10. WHEN a state transition succeeds, THE Actividad_Detail SHALL reload the activity data from the API to reflect the updated estado, instead of only updating the local signal value.

---

### Requirement 7: Restricciones de estilo y arquitectura del módulo

**User Story:** As a frontend developer, I want the Actividades module to be consistent with the project's design system and architecture conventions, so that it integrates cleanly with the rest of the admin shell.

#### Acceptance Criteria

1. THE Actividad_Dashboard, Actividad_List, Actividad_Form, Actividad_Detail and Actividad_Reporte SHALL be implemented as Angular standalone components using `@Component({ standalone: true, ... })`.
2. THE Actividad_Dashboard, Actividad_List, Actividad_Form, Actividad_Detail and Actividad_Reporte SHALL use Angular Signals (`signal()`, `computed()`) for all reactive local state rather than Subject/BehaviorSubject.
3. THE Actividad_Dashboard, Actividad_List, Actividad_Form, Actividad_Detail and Actividad_Reporte SHALL NOT modify any `.ts` file outside of the `actividades` feature folder.
4. THE Actividad_Dashboard, Actividad_List, Actividad_Form, Actividad_Detail and Actividad_Reporte SHALL use exclusively Taiga UI components for form controls, buttons, tabs and overlays; no external UI libraries are permitted.
5. THE Actividad_Dashboard, Actividad_List, Actividad_Form, Actividad_Detail and Actividad_Reporte SHALL apply only Token_CSS values (`--sal-*` or `--tui-*`) for all color properties; hex color literals are forbidden in component `.css` files.
6. THE Actividad_List and Actividad_Dashboard Flush_Tables SHALL use the `.card.flush` + `.table-wrapper.flush` pattern from `styles.css`, matching the visual convention of `alerta-clinica-list`.
7. WHERE a section header is displayed inside a card, THE component SHALL use the `.section-title` pattern (tile icon + h3 + description) consistent with `persona-detail`.
8. THE Actividad_Dashboard SHALL be accessible at the route `/actividades/dashboard` and the Actividad_Reporte at `/actividades/reporte`, consistent with the existing routing structure.
