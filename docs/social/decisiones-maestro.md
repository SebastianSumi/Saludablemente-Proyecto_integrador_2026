> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 21. Deudas técnicas y preguntas abiertas

## 21.1 Deudas registradas

| # | Deuda | Origen | Fase futura |
|---|---|---|---|
| D-01 | Si `personal` habilita edición de `nombres` o `apellidoPaterno`, los `username` cambian y las menciones existentes muestran el username viejo | Contrato `seguridad` v1.2 §2.4 | Cuando `personal` abra edición de identidad |
| D-02 | `fotoUrl` siempre `null` hasta que `seguridad` exponga foto de perfil | Contrato `seguridad` v1.2 §2.3 | Cuando `seguridad` implemente subida de avatar |
| D-03 | `social` no emite `NuevoPostDeEquipoEvent` con destinatarios reales hasta que `personal.api` exponga `miembrosActivosDeEquipo(Long idEquipo)` | Contrato `personal` v1.0 §13 | Enmienda a `personal` |
| D-04 | Live lookup puro asume latencia aceptable de `seguridad`. Si el feed degrada, reconsiderar snapshot híbrido | Decisión de `social` | Según métricas de producción |
| D-05 | Rate limiting de reacciones (toggle) no implementado. Riesgo de spam en notificaciones | `notificaciones` | Cuando `notificaciones` lo requiera |
| D-06 | Notificaciones al compartir un post no definidas | `notificaciones` | Decisión futura |
| D-07 | No hay preview enriquecido de links externos (link previews) | Feature descartada en alcance | Backlog |
| D-08 | Búsqueda full-text con `LIKE`. Si el volumen lo exige, migrar a Oracle Text | Decisión diferida | Según métricas |
| D-09 | No hay índice invertido ni ranking; búsqueda estrictamente textual | Decisión diferida | Backlog |
| D-10 | Comentarios recursivos > 1 nivel no soportados | Decisión cerrada | Backlog |
| D-11 | Adjuntos de comentarios no se borran de MinIO al eliminar el comentario | Decisión de simplicidad | Job de limpieza futuro |
| D-12 | Cifrado de mensajes privados / DMs no implementado | Fuera de alcance | Backlog |

## 21.2 Preguntas abiertas a cerrar antes de F14

| # | Pregunta | Bloquea |
|---|---|---|
| P-01 | Mecanismo de eventos con `notificaciones`: ¿Spring events o RabbitMQ? | F14 |
| P-02 | Formato exacto de tokens de mención en texto: `@usuario`, `@actividad{id}`, `@equipo{id}`? | F8 |
| P-03 | ¿Notificaciones a miembros del equipo al publicar como equipo? Si sí, requiere enmienda a `personal`. | F14, D-03 |
| P-04 | ¿Alcance visible solo a autor/admin o público? Recomendación: autor/admin | F9 |
| P-05 | ¿Compartir un post genera notificación al autor original? | F14 |

## 21.3 Decisiones ya tomadas que deben quedar registradas

- Live lookup puro (no snapshot de autor en POST/COMENTARIO).
- C1 para alcance (contador atómico + `POST_VISTA` purga 6 meses).
- Sin grafo social.
- Sin algoritmo de ranking.
- Comentarios a 1 nivel.
- Reacciones a 7 tipos.
- Un share por usuario por post (regla de negocio).
- Fijados a 3 por scope.
- Moderación dentro de `social` (no delegada a `notificaciones`).
- Posts oficiales se crean solo desde `noticias` con `oficial=true`.
- Adjuntos de comentarios vía 2 pasos (subir → devolver URL → POST con URLs).
- `social` depende de `actividades.api`, `noticias.api`, `personal.api.social`, `seguridad.api.social`, `notificaciones.api`, `shared.storage`.

---

