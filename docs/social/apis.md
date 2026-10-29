> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 11. DTOs

## 11.1 Request

```java
public record CrearPostRequest(
    @Size(max = 150) String titulo,
    @Size(max = 500) String resumen,
    Long idEquipoAutor,               // null = publicar a título personal
    List<@Size(max = 500) String> imagenesUrls
) {}

public record EditarPostRequest(
    @Size(max = 150) String titulo,
    @Size(max = 500) String resumen,
    List<@Size(max = 500) String> imagenesUrls
) {}

public record CrearComentarioRequest(
    @NotNull @Size(max = 1000) String contenido,
    Long idComentarioPadre,
    List<@Size(max = 500) String> adjuntosUrls
) {}

public record EditarComentarioRequest(
    @NotNull @Size(max = 1000) String contenido
) {}

public record ReaccionarRequest(
    @NotNull TipoReaccion tipo
) {}

public record CompartirRequest(
    @Size(max = 500) String contenido,
    Long idEquipoAutor
) {}

public record FijarRequest(
    @NotNull ScopeFijado scopeTipo,
    Long scopeId,
    LocalDateTime fechaExpiracion
) {}

public record ReportarRequest(
    @NotNull MotivoReporte motivo,
    @Size(max = 500) String detalle
) {}

public record ResolverReporteRequest(
    @NotNull EstadoReporte estado,     // ACEPTADO o RECHAZADO
    @Size(max = 500) String comentario
) {}
```

## 11.2 Response

```java
public record PostResumenResponse(
    Long idPost,
    String tipoContenido,
    Long refId,
    Long idEquipoAutor,
    String titulo,
    String resumen,
    String lugar,
    LocalDate fechaEvento,
    String horaInicio,
    String horaFin,
    String estado,
    String imagenUrl,
    String coordinadorNombre,
    Boolean esOficial,
    LocalDateTime fechaPublicacion,
    AutorResponse autor,
    AutorResponse autorEquipo,        // si publicado como equipo
    long totalComentarios,
    Map<String, Long> reaccionesPorTipo,
    long totalReacciones,
    long totalShares,
    String reaccionDelUsuario,
    boolean guardadoPorMi,
    Long postOriginalId               // si es SHARE
) {}

public record PostDetalleResponse(
    Long idPost,
    String tipoContenido,
    Long refId,
    Long idEquipoAutor,
    String titulo,
    String resumen,
    String lugar,
    LocalDate fechaEvento,
    String horaInicio,
    String horaFin,
    String estado,
    String imagenUrl,
    List<String> imagenes,
    String coordinadorNombre,
    Boolean esOficial,
    LocalDateTime fechaPublicacion,
    LocalDateTime fechaActualizacion,
    AutorResponse autor,
    AutorResponse autorEquipo,
    Long alcanceTotal,                // solo autor y admin; null en otro caso
    long totalComentarios,
    long totalShares,
    List<ComentarioResponse> comentarios,
    ReaccionResumenResponse reacciones,
    PostResumenResponse postOriginal, // si es SHARE
    List<MencionResponse> menciones,
    List<String> hashtags
) {}

public record ComentarioResponse(
    Long idComentario,
    Long idComentarioPadre,
    Long idUsuarioAutor,
    AutorResponse autor,
    String contenido,
    Boolean editado,
    LocalDateTime fechaEdicion,
    LocalDateTime fechaComentario,
    List<String> adjuntosUrls,
    List<MencionResponse> menciones,
    List<String> hashtags,
    ReaccionResumenResponse reacciones,
    List<ComentarioResponse> respuestas,  // solo nivel 1
    boolean puedoEditar,
    boolean puedoEliminar
) {}

public record AutorResponse(
    Long idUsuario,
    String username,
    String nombreCompleto,
    String rolPrincipal,
    String nombreEquipo,
    Long idEquipo,
    String fotoUrl                    // siempre null en esta fase
) {}

public record ReaccionResumenResponse(
    long total,
    Map<String, Long> porTipo,
    String reaccionDelUsuario
) {}

public record ReaccionDetalleResponse(
    Long idUsuario,
    AutorResponse autor,
    TipoReaccion tipo,
    LocalDateTime fechaReaccion
) {}

public record MencionResponse(
    TipoMencion tipoMencion,
    Long refId,
    String displayName,
    String avatarUrl,
    String url
) {}

public record ShareResponse(
    Long idPostShare,
    LocalDateTime fechaShare
) {}

public record MuroResponse(
    List<PostResumenResponse> pinned,
    Page<PostResumenResponse> posts
) {}

public record PerfilUsuarioResponse(
    AutorResponse autor,
    List<PostResumenResponse> publicaciones
) {}

public record MiContenidoResponse(
    Page<PostResumenResponse> publicaciones,
    Page<ComentarioResponse> comentarios,
    Page<PostResumenResponse> reacciones,
    Page<PostResumenResponse> guardados
) {}

public record ReporteResponse(
    Long idReporte,
    Long idPost,
    Long idComentario,
    AutorResponse reportante,
    MotivoReporte motivo,
    String detalle,
    LocalDateTime fechaReporte,
    EstadoReporte estado,
    AutorResponse revisor,
    LocalDateTime fechaRevision,
    AccionModeracion accionTomada
) {}

public record MetricaResponse(
    long totalPosts,
    long totalComentarios,
    long totalReacciones,
    long totalShares,
    long usuariosActivos,
    List<TopPost> topComentados,
    List<TopPost> topReaccionados,
    List<TopUsuario> topUsuarios,
    List<TopHashtag> hashtagsTendencia,
    List<VolumenDiario> volumenPorDia
) {}
```

## 11.3 Eventos de dominio (`social/api/eventos/`)

```java
public record ComentarioEnPostEvent(
    Long idPost,
    Long idComentario,
    Long idUsuarioAutor,          // quien comentó
    Long idUsuarioDestinatario,   // autor del post
    LocalDateTime ocurridoEn
) {}

public record RespuestaAComentarioEvent(
    Long idPost,
    Long idComentario,
    Long idComentarioPadre,
    Long idUsuarioAutor,
    Long idUsuarioDestinatario,   // autor del comentario padre
    LocalDateTime ocurridoEn
) {}

public record MencionEvent(
    Long idMencion,
    TipoContenidoMencion tipoContenido,
    Long idContenido,
    Long idUsuarioAutor,
    Long idUsuarioDestinatario,
    LocalDateTime ocurridoEn
) {}

public record ReaccionEnPostEvent(
    Long idPost,
    Long idUsuarioAutor,          // quien reaccionó
    Long idUsuarioDestinatario,   // autor del post
    TipoReaccion tipo,
    LocalDateTime ocurridoEn
) {}

public record NuevoPostDeEquipoEvent(
    Long idPost,
    Long idEquipo,
    Long idUsuarioAutor,
    List<Long> idUsuariosDestinatarios,
    LocalDateTime ocurridoEn
) {}
```

---


# 13. Controllers REST

Todos los endpoints bajo `/api/v1/social`. El usuario autenticado se resuelve vía `SocialUsuarioApi.obtenerUsuarioActual()` en cada controller (una sola vez por request).

## 13.1 Muro

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/muro?tab=PARA_TI&page=0&size=20` | Feed global. Tabs: PARA_TI, RECIENTES, MIS_EQUIPOS, SOLO_ACTIVIDADES, SOLO_NOTICIAS, MIS_PUBLICACIONES, MIS_GUARDADOS |
| `GET` | `/muro/equipos/{idEquipo}?page=0&size=20` | Feed de equipo |
| `GET` | `/muro/actividades/{idActividad}?page=0&size=20` | Feed de actividad |
| `GET` | `/hashtags/{tag}/muro?page=0&size=20` | Feed de hashtag |
| `GET` | `/hashtags/tendencia?dias=7&limite=10` | Hashtags en tendencia |

## 13.2 Posts

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/posts/{idPost}` | Detalle. Registra vista (C1) |
| `POST` | `/posts` | Crear post manual |
| `PATCH` | `/posts/{idPost}` | Editar post propio (solo MANUAL o SHARE) |
| `DELETE` | `/posts/{idPost}` | Eliminar post propio (soft, solo MANUAL o SHARE) |
| `POST` | `/posts/{idPost}/share` | Compartir |
| `POST` | `/posts/{idPost}/guardar` | Guardar |
| `DELETE` | `/posts/{idPost}/guardar` | Quitar guardado |
| `POST` | `/posts/{idPost}/fijar` | Fijar (permisos según scope) |
| `DELETE` | `/posts/{idPost}/fijar?scopeTipo=&scopeId=` | Desfijar |
| `POST` | `/posts/{idPost}/reportar` | Reportar post |

## 13.3 Comentarios

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/posts/{idPost}/comentarios?page=0&size=20` | Listar (raíces + respuestas anidadas) |
| `POST` | `/posts/{idPost}/comentarios` | Crear (raíz o respuesta) |
| `PATCH` | `/posts/{idPost}/comentarios/{idComentario}` | Editar |
| `DELETE` | `/posts/{idPost}/comentarios/{idComentario}` | Eliminar (soft, cascada a respuestas si es raíz) |
| `POST` | `/posts/{idPost}/comentarios/adjuntos` | Subir adjunto (multipart). Devuelve URL |
| `POST` | `/posts/{idPost}/comentarios/{idComentario}/reportar` | Reportar comentario |

## 13.4 Reacciones

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/posts/{idPost}/reacciones` | Reaccionar/actualizar |
| `DELETE` | `/posts/{idPost}/reacciones` | Quitar reacción |
| `GET` | `/posts/{idPost}/reacciones` | Resumen |
| `GET` | `/posts/{idPost}/reacciones/usuarios?tipo=&page=0&size=20` | Ver quién reaccionó |
| `POST` | `/comentarios/{idComentario}/reacciones` | Reaccionar a comentario |
| `DELETE` | `/comentarios/{idComentario}/reacciones` | Quitar |
| `GET` | `/comentarios/{idComentario}/reacciones` | Resumen |
| `GET` | `/comentarios/{idComentario}/reacciones/usuarios?tipo=` | Ver quién reaccionó |

## 13.5 Búsqueda y menciones

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/buscar?q=&tipo=POST\|AUTOR\|HASHTAG&page=0&size=20` | Búsqueda unificada |
| `GET` | `/menciones/buscar?tipo=USUARIO\|ACTIVIDAD\|EQUIPO&q=&limite=5` | Autocompletado |

## 13.6 Perfil y mi contenido

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/usuarios/{idUsuario}/actividad?page=0&size=20` | Perfil público |
| `GET` | `/mi-contenido?tab=PUBLICACIONES\|COMENTARIOS\|REACCIONES\|GUARDADOS&page=0&size=20` | Mi contenido |

## 13.7 Moderación (admin)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/admin/reportes?estado=PENDIENTE&page=0&size=20` | Cola de reportes |
| `PATCH` | `/admin/reportes/{idReporte}` | Resolver (aceptar/rechazar) |
| `PATCH` | `/admin/posts/{idPost}/ocultar` | Ocultar post |
| `PATCH` | `/admin/posts/{idPost}/restaurar` | Restaurar post |
| `PATCH` | `/admin/comentarios/{idComentario}/ocultar` | Ocultar comentario |
| `PATCH` | `/admin/comentarios/{idComentario}/restaurar` | Restaurar comentario |

## 13.8 Analítica (admin)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/admin/metricas?desde=&hasta=` | Panel agregado |

---

