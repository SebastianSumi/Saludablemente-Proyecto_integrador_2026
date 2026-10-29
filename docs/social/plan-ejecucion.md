# Plan de ejecución por fases — no iniciado

El contenido literal F0–F18 está en fases.md. Sus ~32 h son una estimación del maestro, no un compromiso validado: no incluye un desglose frontend ni el cierre de brechas. Conservar identificadores, pero no ejecutar una fase sin sus prerrequisitos reales.

| Fase | Prerrequisitos y entrega verificable | Validación / frontend propuesto |
|---|---|---|
| F0 | C02/C08/C09 cerrados; esquema y límites acordados sin resetear BD | Provisión revisada y límites Modulith |
| F1 | API social y DTOs exactos del maestro; sin inventar firmas externas | Revisión de exposición API y DTOs |
| F2 | Modelo 13 tablas, enums y PKs; C10/C15 resueltos | Oracle constraints, null, unicidad y repositorios |
| F3 | Contratos acordados y adaptación mínima necesaria de F4; sincronización idempotente | HU01–03; doble llamada conserva ID y no duplica |
| F4 | Dueños de identidad/equipos/actividad/noticia; C03/C04/C07 | Adaptadores, batch y errores; sin importar internos |
| F5 | F3/F4; entidades auxiliares de pins antes de usarlas; definir cupos C16 | Muro, filtros, FijadoService y CRUD manual/edición/borrado con HU26/34–36; pantalla de feed/editor |
| F6 | C05/C11; almacenamiento real y tokens de adjuntos | Hilos, adjuntos, permisos y tombstones; detalle Angular |
| F7 | F2 y autorización activa | Reacciones y quién reaccionó; selector/lista |
| F8 | C17 y contratos de búsqueda; entidades hashtag/mención | Parsers y edición; autocomplete/navegación |
| F9 | C12/C13/C15; ampliar CRUD a SHARE | Shares, favoritos, contador C1, purga; acciones y alcance |
| F10 | C14 y contrato de respuesta de búsqueda | Resultados de todas las categorías; pantalla búsqueda |
| F11 | F5–F10 y política de visibilidad | Perfiles y tabs; aislamiento de guardados |
| F12 | Autorización admin; tablas reporte y reglas de restauración | Reporte duplicado 409, moderación; panel admin |
| F13 | Definiciones de métricas confirmadas | Agregados verificables; panel Angular |
| F14 | C18/C19, P-01/P-03/P-05 y contrato destinatarios | AFTER_COMMIT, rollback sin aviso, sin autoavisos; deep links |
| F15 | Dueño actividades y C08; diez puntos del maestro verificados en código | Actualización por estados/logo/galería/coordinador |
| F16 | Dueño noticias, campo oficial y C08 | Noticia oficial, sincronización y auto-pin |
| F17 | Opcional, solo dev, idempotente | Ningún dato de ejemplo en otros perfiles |
| F18 | Flujos backend y frontend anteriores; decisiones cerradas | Build, pruebas, Swagger, modules.verify y recorrido E2E |

Entidades que excedan las «base» de F2 deben incorporarse antes de su servicio consumidor: PostFijado F5; adjuntos F6; hashtag/mención F8; vista/guardado F9; reporte F12. No permitir servicios sobre tablas todavía inexistentes. Los contratos requeridos por F3 deben acordarse antes de F3 aunque F4 conserve su número.

Al terminar cada fase: registrar archivos, HU/RN, decisiones, pruebas y resultado en estado.md. Mantener las otras fases pendientes. Ante un contrato ausente, preparar la propuesta documental para el dueño; no introducir un mock productivo como solución definitiva.