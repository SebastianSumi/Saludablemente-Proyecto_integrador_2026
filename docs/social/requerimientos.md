> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 1. Resumen ejecutivo

| Aspecto | Detalle |
|---|---|
| **Qué es** | Red social institucional: feed de publicaciones (actividades, noticias, manuales, shares) con comentarios anidados, reacciones, menciones, hashtags, moderación y analítica |
| **Dueño del módulo** | (nuevo desarrollador) |
| **Depende de** | `seguridad.api.social`, `personal.api.social`, `actividades.api`, `noticias.api`, `notificaciones.api`, `shared.storage` |
| **Es consumido por** | `actividades` (sincronización de posts de actividad), `noticias` (sincronización de posts de noticia) |
| **Mecanismo de creación de posts de actividad/noticia** | Llamada directa desde el módulo dueño a `social.api.SocialApiService.sincronizarPostDeActividad(...)` / `sincronizarPostDeNoticia(...)` |
| **Mecanismo de notificación** | Eventos de dominio emitidos por `social` y consumidos por `notificaciones` |
| **Autenticación** | JWT emitido por Keycloak, validado y resuelto por el módulo `seguridad` |
| **Almacenamiento de imágenes** | URLs de MinIO. `social` sube solo adjuntos de comentarios bajo prefijo `social/comentarios/{idPost}/`. Referencia URLs de actividades y noticias sin re-subirlas |
| **Base de datos** | Esquema `SLB_SOCIAL` en Oracle 23ai Free |
| **Estrategia de autores** | Live lookup puro contra `seguridad` (sin snapshot en BD) |
| **Estrategia de alcance (vistas)** | C1: contador atómico `POST.ALCANCE_TOTAL` + tabla `POST_VISTA` con purga a 6 meses |
| **Algoritmo de orden** | Cronológico por `FECHA_PUBLICACION DESC`. Sin algoritmo de ranking |
| **Grafo social** | No existe. Sin "seguir" a personas. Solo filtros por equipo y por actividad |

---


# 2. Alcance funcional

## 2.1 Features confirmadas (todas entran)

### A. Identidad y autoría

| # | Feature |
|---|---|
| A.1 | Autor enriquecido con rol y equipo (`Coordinador · Salud Mental`) resuelto en vivo contra `seguridad` |
| A.2 | Perfil público navegable del usuario (posts y comentarios visibles) |
| A.3 | Editar post propio (título, resumen, imágenes) |
| A.4 | Eliminar post propio (soft delete, `activo=false`) |
| A.5 | Editar comentario propio (marca `editado=true` + `fechaEdicion`) |
| A.6 | Eliminar comentario propio o ajeno siendo admin (soft delete) |
| A.7 | Posts manuales a título personal |
| A.8 | Posts manuales en nombre de un equipo (con permisos por membresía) |
| A.9 | Compartir (share) un post existente con texto opcional |
| A.10 | Posts auto-generados desde actividades |
| A.11 | Posts auto-generados desde noticias oficiales |

### B. Interacción

| # | Feature |
|---|---|
| B.1 | Comentarios anidados (1 nivel: raíz + respuestas) |
| B.2 | Reacciones a comentarios |
| B.3 | Reacciones ampliadas a posts (7 tipos) |
| B.4 | Ver quién reaccionó (lista paginada, filtrable por tipo) |
| B.5 | Ver quién comentó (lista ya existente) |
| B.6 | Fotos en comentarios (hasta 4 adjuntos) |
| B.7 | Menciones `@usuario`, `@actividad`, `@equipo` con autocompletado |

### C. Descubrimiento

| # | Feature |
|---|---|
| C.1 | Búsqueda unificada (posts, comentarios, autores, hashtags) |
| C.2 | Filtros de feed (tabs de scope) |
| C.3 | Hashtags navegables |
| C.4 | Feed por equipo |
| C.5 | Feed por actividad |
| C.6 | Feed por hashtag |
| C.7 | Publicaciones fijadas (global, por equipo, por actividad) |

### D. Retención

| # | Feature |
|---|---|
| D.1 | Guardar / favoritos (privados) |
| D.2 | Vista de autor "mi contenido" (posts, comentarios, reacciones, guardados) |
| D.3 | Contadores visibles (reacciones, comentarios, shares, alcance) |

### E. Institucional

| # | Feature |
|---|---|
| E.1 | Anuncios oficiales (posts de noticias marcadas como `oficial=true`) |

### F. Moderación

| # | Feature |
|---|---|
| F.1 | Reportar post o comentario (motivo + detalle) |
| F.2 | Panel de moderación para admin (cola de reportes, aceptar/rechazar, ocultar/restaurar) |

### G. Analítica

| # | Feature |
|---|---|
| G.1 | Panel de métricas para admin (posts más comentados, usuarios más activos, hashtags en tendencia, engagement) |

### H. Notificaciones

| # | Feature |
|---|---|
| H.1 | Emisión de eventos de dominio hacia el módulo `notificaciones` en: comentario en post, respuesta a comentario, mención, reacción en post, nuevo post de equipo |

## 2.2 Features descartadas (decisión cerrada)

| Feature | Decisión |
|---|---|
| Grafo social / "seguir" usuarios | Descartado |
| Algoritmo de orden / "Para ti" con ranking | Descartado. "Para ti" es cronológico |
| Comentarios recursivos > 1 nivel | Descartado |
| Notificaciones construidas en `social` | Descartado. `social` consume `notificaciones` existente |
| Edición de `username` | Descartado en esta fase (deuda futura) |
| Fotos de perfil desde `social` | Descartado. Se resuelve vía `seguridad` (fotoUrl siempre `null` en esta fase) |

---


# 5. Requerimientos funcionales detallados

## 5.1 Historias de usuario (todas las features)

| # | Historia | Feature |
|---|---|---|
| HU-01 | Cuando se crea una actividad, aparece automáticamente como publicación en el muro social | A.10 |
| HU-02 | Cuando una actividad cambia de estado o se reprograma, su publicación se actualiza | A.10 |
| HU-03 | Cuando se publica una noticia oficial, aparece automáticamente como publicación destacada | A.11, E.1 |
| HU-04 | Cualquier usuario autenticado ve el muro paginado por fecha descendente | C.2 |
| HU-05 | Cualquier usuario autenticado comenta una publicación (raíz o respuesta) | B.1 |
| HU-06 | Cualquier usuario elimina sus propios comentarios | A.6 |
| HU-07 | Un administrador elimina cualquier comentario | A.6, F.2 |
| HU-08 | Cualquier usuario edita sus propios comentarios (marca "editado") | A.5 |
| HU-09 | Cualquier usuario reacciona con LIKE, LOVE, HAHA, WOW, SAD, APLAUSO o ENOJADO | B.3 |
| HU-10 | Un usuario solo tiene una reacción activa por post o por comentario | B.2, B.3 |
| HU-11 | Cambiar de reacción reemplaza la anterior | B.2, B.3 |
| HU-12 | Cualquier usuario puede compartir un post con texto opcional | A.9 |
| HU-13 | Cada publicación muestra: título, resumen, logo, fecha, lugar, estado, autor enriquecido, contadores | D.3 |
| HU-14 | Cada comentario muestra autor enriquecido, texto, fecha, adjuntos | B.1, B.6 |
| HU-15 | Cada publicación muestra el desglose de reacciones y el total | D.3 |
| HU-16 | Cualquier usuario ve quién reaccionó a un post o comentario | B.4 |
| HU-17 | Cualquier usuario ve el perfil público de un autor con sus posts y comentarios | A.2 |
| HU-18 | Cualquier usuario navega su propio perfil con tabs "Mis publicaciones / Mis comentarios / Mis reacciones / Mis guardados" | D.2 |
| HU-19 | Cualquier usuario guarda un post para verlo luego | D.1 |
| HU-20 | Cualquier usuario busca posts, comentarios, autores y hashtags | C.1 |
| HU-21 | Cualquier usuario usa hashtags navegables | C.3 |
| HU-22 | Cualquier usuario filtra el feed por scope (equipo, actividad, etc.) | C.2 |
| HU-23 | Cualquier usuario navega el feed de un equipo | C.4 |
| HU-24 | Cualquier usuario navega el feed de una actividad | C.5 |
| HU-25 | Un administrador fija hasta 3 posts por scope (global, equipo, actividad) | C.7 |
| HU-26 | Un usuario publica a título personal o en nombre de un equipo del que es miembro | A.7, A.8 |
| HU-27 | Un usuario menciona `@usuario`, `@actividad`, `@equipo` con autocompletado | B.7 |
| HU-28 | Cualquier usuario reporta un post o comentario con motivo | F.1 |
| HU-29 | Un administrador ve la cola de reportes y resuelve (aceptar/rechazar) | F.2 |
| HU-30 | Un administrador oculta o restaura un post | F.2 |
| HU-31 | Un administrador ve métricas agregadas del módulo | G.1 |
| HU-32 | El autor y el admin ven el alcance (vistas únicas) de un post | D.3 |
| HU-33 | Cualquier usuario recibe notificación al ser mencionado, al comentar en su post, al responder su comentario, al reaccionar a su post, o al publicar su equipo | H.1 |
| HU-34 | Un usuario edita el texto y adjuntos de su propio post | A.3 |
| HU-35 | Un usuario elimina su propio post manual (soft delete) | A.4 |
| HU-36 | Un usuario no puede eliminar ni editar un post auto-generado (de actividad o noticia) | A.3, A.4 |

## 5.2 Reglas de negocio

| # | Regla |
|---|---|
| RN-01 | `POST(TIPO_CONTENIDO='ACTIVIDAD', REF_ID)` es única. Idempotencia de sincronización |
| RN-02 | `POST(TIPO_CONTENIDO='NOTICIA', REF_ID)` es única. Idempotencia de sincronización |
| RN-03 | `sincronizarPostDeActividad` / `sincronizarPostDeNoticia` son idempotentes: si existe, actualiza; si no, crea |
| RN-04 | Un comentario eliminado no se borra de BD: `activo = false` |
| RN-05 | Una reacción eliminada sí se borra físicamente (PK compuesta) |
| RN-06 | Un comentario solo lo elimina su autor o un administrador |
| RN-07 | Un post con `REF_ESTADO='CANCELADA'` sigue visible |
| RN-08 | El muro solo muestra publicaciones con `activo = true` |
| RN-09 | Un usuario solo tiene una reacción por post (PK compuesta) |
| RN-10 | Un usuario solo tiene una reacción por comentario (PK compuesta) |
| RN-11 | Cambiar de reacción actualiza `tipo` y `fecha_reaccion` |
| RN-12 | Un comentario solo tiene 1 nivel de anidación. `ID_COMENTARIO_PADRE` puede ser NULL (raíz) o apuntar a un comentario raíz. Nunca a un comentario hijo |
| RN-13 | Al eliminar un comentario raíz con respuestas, se marcan `activo=false` en cascada. El padre se renderiza como "Comentario eliminado" si tenía respuestas |
| RN-14 | Los posts con `TIPO_CONTENIDO='ACTIVIDAD'` o `'NOTICIA'` **no** son editables ni eliminables por el usuario. Solo se actualizan vía sincronización o por admin (ocultar) |
| RN-15 | Un usuario solo puede publicar "como equipo" si es miembro del equipo (`esMiembroDeEquipo`) |
| RN-16 | Un post "como equipo" puede ser editado por el autor original o por un admin del equipo o admin global |
| RN-17 | Los posts `TIPO_CONTENIDO='SHARE'` apuntan a un `ORIGEN_POST_ID` que **nunca** es otro SHARE. Se resuelve al root original |
| RN-18 | Un usuario solo puede compartir un post si el original está `activo=true` |
| RN-19 | Los shares incrementan `POST.SHARES_TOTAL` del original |
| RN-20 | Un post se puede fijar hasta 3 veces por scope. Al intentar un cuarto, 409 |
| RN-21 | Los hashtags se normalizan a lowercase y aceptan `[a-z0-9_áéíóúñ]`, longitud 2-50 |
| RN-22 | Las menciones inexistentes o de usuarios inactivos se ignoran silenciosamente al guardar |
| RN-23 | Las menciones se re-parsean al editar el comentario/post (borrar + insertar) |
| RN-24 | Un reporte tiene uno de dos targets: `ID_POST` o `ID_COMENTARIO`. Nunca ambos |
| RN-25 | Un usuario puede reportar el mismo contenido una sola vez. Segundo reporte → 409 |
| RN-26 | Al aceptar un reporte, se oculta el contenido (`activo=false`) y se registra `ACCION_TOMADA='OCULTADO'` |
| RN-27 | El alcance (`ALCANCE_TOTAL`) se incrementa solo cuando un usuario ve el post por primera vez |
| RN-28 | El autor del post y los admins no cuentan como vista |
| RN-29 | El alcance se actualiza solo en `GET /posts/{id}` (no en el muro) |
| RN-30 | La tabla `POST_VISTA` se purga mensualmente a 180 días. `ALCANCE_TOTAL` no se recalcula |
| RN-31 | El número de alcance es visible solo para autor y admin |
| RN-32 | Los guardados son privados: no hay contador público de guardados |
| RN-33 | Los posts oficiales (`ES_OFICIAL=true`) se destacan visualmente y se pueden auto-fijar al tope del muro global por tiempo configurable |
| RN-34 | Un usuario inactivo en `seguridad` no puede crear, editar, comentar, reaccionar ni reportar. `social` rechaza con `BusinessRuleException` |
| RN-35 | El feed "Para ti" y "Recientes" son idénticos (cronológico descendente). "Para ti" queda reservado para algoritmo futuro |
| RN-36 | El feed por equipo y por actividad muestra solo los pins de su scope |
| RN-37 | Un share se cuenta como un post en el feed. Su contenido se renderiza a partir del original |
| RN-38 | El autor de un post con `TIPO_CONTENIDO='SHARE'` es el usuario que compartió, no el autor original |
| RN-39 | Los posts oficiales solo se crean desde noticias con `oficial=true`. `social` no permite crear posts con `ES_OFICIAL=true` manualmente |
| RN-40 | Un post manual requiere al menos `TITULO` y `RESUMEN`, o al menos 1 imagen |

---

