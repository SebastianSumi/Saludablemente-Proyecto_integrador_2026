> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 3. Contexto arquitectónico

## 3.1 Stack

- Java 21 LTS
- Spring Boot 4.0.7
- Spring Modulith (verificación de límites entre módulos)
- Spring Data JPA + Oracle Driver
- Spring Security OAuth2 Resource Server (consumido indirectamente vía `seguridad`)
- SpringDoc OpenAPI (Swagger)
- Lombok
- MinIO (almacenamiento de archivos)
- Keycloak (autenticación, gestionado por `seguridad`)
- RabbitMQ (notificaciones, futuro)
- Oracle 23ai Free en `FREEPDB1`

## 3.2 Base de datos — esquemas

| Esquema | Módulo dueño |
|---|---|
| `SLB_PERSONAL` | Personal (+ Equipos) |
| `SLB_ACTIVIDADES` | Actividades |
| `SLB_NOTICIAS` | Noticias |
| `SLB_SEGURIDAD` | Seguridad |
| `SLB_SOCIAL` | **Social** |

- La app se conecta como `SALUDABLEMENTE_APP` con privilegios sobre todos los esquemas.
- `ddl-auto: update` en `application-dev.yml`.
- `default_schema: SLB_PERSONAL` → cada entidad de `social` debe declarar `@Table(name="X", schema="SLB_SOCIAL")` explícitamente.

## 3.3 Organización Modulith

```
upeu.edu.pe.Saludabemente/
├── actividades/      → dueño: Pedro
├── personal/         → dueño: Sebastián
├── noticias/         → dueño: Francisco
├── seguridad/        → transversal (equipo)
├── notificaciones/   → existente (equipo)
├── shared/           → transversal @ApplicationModule(type = OPEN)
└── social/           → NUEVO
```

Regla: lo que un módulo expone a otros vive en `{modulo}/api/` con `@NamedInterface` en `package-info.java`. Lo demás es privado.

Excepciones globales en `upeu.edu.pe.Saludabemente.exception.*`:
- `BusinessRuleException` → HTTP 409
- `ResourceNotFoundException` → HTTP 404
- Traducidas por un `GlobalExceptionHandler` central.

---


# 4. Límites del módulo `social` (Modulith)

## 4.1 Grafo de dependencias

```
                       ┌─────────────────────────────┐
                       │   social.api                │
                       │   (@NamedInterface)         │
                       │   SocialApiService          │
                       └─────────────┬───────────────┘
                                     │
              ┌──────────────────────┼───────────────────────┐
              │                      │                       │
      actividades.api        noticias.api           (otros futuros)
              │                      │
              └──── llaman a social.api (unidireccional) ────►

  social ──► seguridad.api.social       (identidad, autor, menciones @usuario)
  social ──► personal.api.social        (equipos, membresías, menciones @equipo)
  social ──► actividades.api            (snapshots, búsqueda, @actividad, feed)
  social ──► noticias.api               (snapshots, búsqueda, @noticia)
  social ──► notificaciones.api         (publicar eventos)
  social ──► shared.storage             (adjuntos de comentarios)
```

## 4.2 Reglas duras

1. `social` **solo** importa `*.api.*` de otros módulos. Nunca clases internas.
2. Otros módulos **solo** llaman a `social.api.SocialApiService`. Nunca clases internas de `social`.
3. `social` **no** accede a repositorios ni entidades de otros módulos. Solo a métodos públicos.
4. `social` **no** hace JOIN a tablas de otros esquemas.
5. `social` **no** almacena entidades de otros módulos como `@ManyToOne`. Solo guarda IDs (`Long`).

## 4.3 `package-info.java` del módulo

```java
@org.springframework.modulith.ApplicationModule(
    displayName = "Social",
    allowedDependencies = {
        "seguridad-api-social",
        "personal-api-social",
        "actividades-api", "actividades-api-dto",
        "noticias-api", "noticias-api-dto",
        "notificaciones-api", "notificaciones-api-dto",
        "shared-storage"
    }
)
package upeu.edu.pe.Saludabemente.social;
```

## 4.4 NamedInterfaces expuestas por `social`

```java
// social/api/package-info.java
@NamedInterface("social-api")
package upeu.edu.pe.Saludabemente.social.api;

// social/api/dto/package-info.java
@NamedInterface("social-api-dto")
package upeu.edu.pe.Saludabemente.social.api.dto;

// social/api/eventos/package-info.java
@NamedInterface("social-api-eventos")
package upeu.edu.pe.Saludabemente.social.api.eventos;
```

---


# 8. Repositorios

```java
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByTipoContenidoAndRefId(TipoContenidoPost tipo, Long refId);

    Page<Post> findByActivoTrueOrderByFechaPublicacionDesc(Pageable pageable);
    Page<Post> findByTipoContenidoAndActivoTrueOrderByFechaPublicacionDesc(TipoContenidoPost tipo, Pageable pageable);
    Page<Post> findByActivoTrueAndAutorIdOrderByFechaPublicacionDesc(Long autorId, Pageable pageable);
    Page<Post> findByActivoTrueAndIdEquipoAutorOrderByFechaPublicacionDesc(Long idEquipo, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.activo = true
          AND p.idEquipoAutor IN (
              SELECT m.idEquipo FROM MiembroEquipoView m WHERE m.idUsuario = :idUsuario
          )
        ORDER BY p.fechaPublicacion DESC
    """)
    Page<Post> findByEquiposDeUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.activo = true
          AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.resumen) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY p.fechaPublicacion DESC
    """)
    Page<Post> buscarPorTexto(@Param("q") String q, Pageable pageable);

    List<Post> findByActivoTrueAndIdPostIn(Collection<Long> ids);
}

public interface PostImagenRepository extends JpaRepository<PostImagen, Long> {
    List<PostImagen> findByIdPostOrderByOrdenAsc(Long idPost);

    @Modifying
    @Query("DELETE FROM PostImagen i WHERE i.idPost = :idPost")
    void deleteByIdPost(@Param("idPost") Long idPost);
}

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
    List<PostHashtag> findByIdPost(Long idPost);
    @Modifying @Query("DELETE FROM PostHashtag h WHERE h.idPost = :idPost")
    void deleteByIdPost(@Param("idPost") Long idPost);

    Page<PostHashtag> findByTagOrderByIdPostDesc(String tag, Pageable pageable);
    @Query("SELECT h.tag, COUNT(h) FROM PostHashtag h GROUP BY h.tag ORDER BY COUNT(h) DESC")
    List<Object[]> topHashtags(Pageable pageable);
}

public interface PostVistaRepository extends JpaRepository<PostVista, Long> {
    Optional<PostVista> findByIdPostAndIdUsuario(Long idPost, Long idUsuario);

    @Modifying
    @Query("DELETE FROM PostVista v WHERE v.fechaUltimaVista < :corte")
    int purgarAntiguas(@Param("corte") LocalDateTime corte);

    @Query("""
        SELECT COUNT(v) FROM PostVista v
        WHERE v.idPost = :idPost AND v.fechaUltimaVista >= :desde
    """)
    long contarDesde(@Param("idPost") Long idPost, @Param("desde") LocalDateTime desde);
}

public interface PostGuardadoRepository extends JpaRepository<PostGuardado, PostGuardadoId> {
    boolean existsByIdPostAndIdUsuario(Long idPost, Long idUsuario);
    Page<PostGuardado> findByIdUsuarioOrderByFechaGuardadoDesc(Long idUsuario, Pageable pageable);
    void deleteByIdPostAndIdUsuario(Long idPost, Long idUsuario);
}

public interface PostFijadoRepository extends JpaRepository<PostFijado, Long> {
    List<PostFijado> findByScopeTipoAndScopeIdOrderByFechaFijadoDesc(ScopeFijado tipo, Long scopeId);
    List<PostFijado> findByScopeTipoOrderByFechaFijadoDesc(ScopeFijado tipo);
    long countByScopeTipoAndScopeId(ScopeFijado tipo, Long scopeId);
    Optional<PostFijado> findByIdPostAndScopeTipoAndScopeId(Long idPost, ScopeFijado tipo, Long scopeId);
    void deleteByIdPostAndScopeTipoAndScopeId(Long idPost, ScopeFijado tipo, Long scopeId);
}

public interface PostReporteRepository extends JpaRepository<PostReporte, Long> {
    Page<PostReporte> findByEstadoOrderByFechaReporteAsc(EstadoReporte estado, Pageable pageable);
    boolean existsByIdUsuarioReportaAndIdPost(Long idUsuario, Long idPost);
    boolean existsByIdUsuarioReportaAndIdComentario(Long idUsuario, Long idComentario);
}

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByIdPostAndActivoTrueAndIdComentarioPadreIsNullOrderByFechaComentarioAsc(Long idPost);
    List<Comentario> findByIdPostAndActivoTrueAndIdComentarioPadreInOrderByFechaComentarioAsc(Long idPost, List<Long> padres);
    long countByIdPostAndActivoTrue(Long idPost);
    long countByIdPostAndIdComentarioPadreAndActivoTrue(Long idPost, Long idPadre);
    Page<Comentario> findByIdUsuarioAutorAndActivoTrueOrderByFechaComentarioDesc(Long idUsuario, Pageable pageable);

    @Query("""
        SELECT c.idPost, COUNT(c) FROM Comentario c
        WHERE c.idPost IN :ids AND c.activo = true
        GROUP BY c.idPost
    """)
    List<Object[]> countByPosts(@Param("ids") Collection<Long> ids);
}

public interface ComentarioAdjuntoRepository extends JpaRepository<ComentarioAdjunto, Long> {
    List<ComentarioAdjunto> findByIdComentarioOrderByOrdenAsc(Long idComentario);
}

public interface ComentarioHashtagRepository extends JpaRepository<ComentarioHashtag, Long> {
    List<ComentarioHashtag> findByIdComentario(Long idComentario);
    @Modifying @Query("DELETE FROM ComentarioHashtag h WHERE h.idComentario = :id")
    void deleteByIdComentario(@Param("id") Long idComentario);
}

public interface MencionRepository extends JpaRepository<Mencion, Long> {
    List<Mencion> findByTipoContenidoAndIdContenido(TipoContenidoMencion tipo, Long idContenido);
    @Modifying
    @Query("DELETE FROM Mencion m WHERE m.tipoContenido = :tipo AND m.idContenido = :id")
    void deleteByContenido(@Param("tipo") TipoContenidoMencion tipo, @Param("id") Long idContenido);
    List<Mencion> findByTipoMencionAndRefId(TipoMencion tipo, Long refId);
}

public interface ReaccionPostRepository extends JpaRepository<ReaccionPost, ReaccionPostId> {
    Optional<ReaccionPost> findByIdPostAndIdUsuario(Long idPost, Long idUsuario);

    @Query("""
        SELECT r.tipo, COUNT(r) FROM ReaccionPost r
        WHERE r.idPost = :idPost
        GROUP BY r.tipo
    """)
    List<Object[]> contarPorTipo(@Param("idPost") Long idPost);

    @Query("""
        SELECT r.idPost, r.tipo, COUNT(r) FROM ReaccionPost r
        WHERE r.idPost IN :ids
        GROUP BY r.idPost, r.tipo
    """)
    List<Object[]> contarPorTipoBatch(@Param("ids") Collection<Long> ids);

    @Query("""
        SELECT r FROM ReaccionPost r
        WHERE r.idPost IN :postIds AND r.idUsuario = :idUsuario
    """)
    List<ReaccionPost> findByPostIdsAndUsuario(@Param("postIds") Collection<Long> postIds,
                                               @Param("idUsuario") Long idUsuario);

    Page<ReaccionPost> findByIdPostOrderByFechaReaccionDesc(Long idPost, Pageable pageable);
    Page<ReaccionPost> findByIdPostAndTipoOrderByFechaReaccionDesc(Long idPost, TipoReaccion tipo, Pageable pageable);

    Page<ReaccionPost> findByIdUsuarioOrderByFechaReaccionDesc(Long idUsuario, Pageable pageable);
}

public interface ReaccionComentarioRepository extends JpaRepository<ReaccionComentario, ReaccionComentarioId> {
    Optional<ReaccionComentario> findByIdComentarioAndIdUsuario(Long idComentario, Long idUsuario);

    @Query("""
        SELECT r.tipo, COUNT(r) FROM ReaccionComentario r
        WHERE r.idComentario = :id
        GROUP BY r.tipo
    """)
    List<Object[]> contarPorTipo(@Param("id") Long idComentario);

    Page<ReaccionComentario> findByIdComentarioOrderByFechaReaccionDesc(Long idComentario, Pageable pageable);
}
```

---


# 9. Servicios

## 9.1 Interfaces y responsabilidades

| Servicio | Responsabilidad |
|---|---|
| `PostService` | CRUD de posts manuales, edición, eliminación, listado mi contenido |
| `PostServiceImpl` (implements `SocialApiService` + `PostService`) | Sincronización desde actividades/noticias, CRUD, share |
| `ComentarioService` | Crear, editar, listar (anidado), eliminar (cascada soft-delete), adjuntos |
| `ReaccionService` | Reaccionar/quitar en post y en comentario, resumen, lista de quién reaccionó |
| `MuroService` | Feed global con tabs, feed por equipo, por actividad, por hashtag, mezcla con fijados |
| `HashtagService` | Parser de hashtags, índice, trending |
| `MencionService` | Parser, resolución contra `seguridad`/`personal`/`actividades`, persistencia |
| `BusquedaService` | Búsqueda unificada (posts, comentarios, autores, hashtags) |
| `PerfilActividadService` | Perfil público y "mi contenido" |
| `ModeracionService` | Reportes, aceptar/rechazar, ocultar/restaurar |
| `AnaliticaService` | Métricas agregadas para admin |
| `ShareService` | Crear post SHARE, incrementar contador del original |
| `GuardadoService` | Guardar/quitar, listar guardados propios |
| `FijadoService` | Fijar/desfijar, respetar límite de 3 por scope, verificar permisos |
| `AlcanceService` | Registrar vista (C1), calcular alcance visible, purgar |

## 9.2 Capa anticorrupción (client services)

| Client | Delega en |
|---|---|
| `SocialUsuarioClientService` | `seguridad.api.social.SocialUsuarioApi` |
| `SocialEquipoClientService` | `personal.api.social.SocialEquipoApi` |
| `ActividadClientService` | `actividades.api` (pendiente contrato) |
| `NoticiaClientService` | `noticias.api` (pendiente contrato) |
| `NotificacionClientService` | `notificaciones.api` o `ApplicationEventPublisher` (pendiente definir) |

## 9.3 Manejo de usuario inactivo (live lookup)

`SocialUsuarioClientService` captura `ResourceNotFoundException` al resolver autores de un feed y devuelve un placeholder:

```java
private SocialUsuarioSnapshotDto placeholder(Long idUsuario) {
    return new SocialUsuarioSnapshotDto(
        idUsuario,
        "eliminado_" + idUsuario,   // no matchea regex @([a-z0-9\\.]+)
        null,
        "[Usuario eliminado]",
        null,
        List.of(),
        null,
        null,
        false
    );
}
```

Se usa solo en render. Nunca se persiste.

---


# 12. Mappers

```java
@Component
public class PostMapper {

    public PostResumenResponse toResumen(
        Post p, AutorResponse autor, AutorResponse autorEquipo,
        long totalComentarios, Map<String, Long> reaccionesPorTipo,
        long totalReacciones, long totalShares, String reaccionDelUsuario,
        boolean guardadoPorMi
    ) { ... }

    public PostDetalleResponse toDetalle(
        Post p, AutorResponse autor, AutorResponse autorEquipo,
        Long alcanceVisible, List<ComentarioResponse> comentarios,
        ReaccionResumenResponse reacciones, PostResumenResponse postOriginal,
        List<MencionResponse> menciones, List<String> hashtags
    ) { ... }

    public ComentarioResponse toComentario(
        Comentario c, AutorResponse autor, List<String> adjuntos,
        List<MencionResponse> menciones, List<String> hashtags,
        ReaccionResumenResponse reacciones, List<ComentarioResponse> respuestas,
        boolean puedoEditar, boolean puedoEliminar
    ) { ... }
}

@Component
public class AutorMapper {
    // Delega en SocialUsuarioSnapshotDto / SocialEquipoSnapshotDto
    // y construye AutorResponse con fallback [Usuario eliminado]
}
```

---


# 19. Estructura de carpetas del módulo

```
src/main/java/upeu/edu/pe/Saludabemente/social/
│
├── package-info.java                          # @ApplicationModule
├── SocialModuleConfiguration.java             # @Configuration (beans del módulo)
│
├── api/                                       # EXPUESTO a otros módulos
│   ├── SocialApiService.java
│   ├── package-info.java                      # @NamedInterface("social-api")
│   ├── impl/
│   │   └── SocialApiServiceImpl.java
│   ├── dto/
│   │   ├── ActividadSocialDto.java
│   │   ├── NoticiaSocialDto.java
│   │   └── package-info.java                  # @NamedInterface("social-api-dto")
│   └── eventos/
│       ├── ComentarioEnPostEvent.java
│       ├── RespuestaAComentarioEvent.java
│       ├── MencionEvent.java
│       ├── ReaccionEnPostEvent.java
│       ├── NuevoPostDeEquipoEvent.java
│       └── package-info.java                  # @NamedInterface("social-api-eventos")
│
├── config/
│   ├── SocialConfig.java                      # @Configuration (bean config)
│   └── SocialProperties.java                  # @ConfigurationProperties
│
├── post/
│   ├── controller/
│   │   ├── PostController.java
│   │   ├── MuroController.java
│   │   ├── ShareController.java
│   │   └── GuardadoController.java
│   ├── dto/
│   │   ├── request/...
│   │   └── response/...
│   ├── entity/
│   │   ├── Post.java
│   │   ├── PostImagen.java
│   │   ├── PostHashtag.java
│   │   ├── PostVista.java
│   │   ├── PostGuardado.java
│   │   ├── PostGuardadoId.java
│   │   ├── PostFijado.java
│   │   └── PostReporte.java
│   ├── mapper/
│   │   └── PostMapper.java
│   ├── repository/... (8 repositorios)
│   └── service/
│       ├── PostService.java
│       ├── MuroService.java
│       ├── ShareService.java
│       ├── GuardadoService.java
│       ├── FijadoService.java
│       ├── AlcanceService.java
│       └── impl/...
│
├── comentario/
│   ├── controller/
│   │   ├── ComentarioController.java
│   │   └── AdjuntoController.java
│   ├── dto/...
│   ├── entity/
│   │   ├── Comentario.java
│   │   ├── ComentarioAdjunto.java
│   │   └── ComentarioHashtag.java
│   ├── mapper/...
│   ├── repository/...
│   └── service/
│       ├── ComentarioService.java
│       └── impl/...
│
├── reaccion/
│   ├── controller/
│   │   └── ReaccionController.java
│   ├── dto/...
│   ├── entity/
│   │   ├── ReaccionPost.java
│   │   ├── ReaccionPostId.java
│   │   ├── ReaccionComentario.java
│   │   ├── ReaccionComentarioId.java
│   │   └── TipoReaccion.java
│   ├── mapper/...
│   ├── repository/...
│   └── service/...
│
├── menciones/
│   ├── controller/
│   │   └── MencionController.java
│   ├── dto/...
│   ├── entity/
│   │   ├── Mencion.java
│   │   ├── TipoMencion.java
│   │   └── TipoContenidoMencion.java
│   ├── repository/
│   │   └── MencionRepository.java
│   └── service/
│       ├── MencionParser.java
│       ├── MencionService.java
│       └── impl/...
│
├── hashtag/
│   ├── service/
│   │   ├── HashtagParser.java
│   │   ├── HashtagService.java
│   │   └── impl/...
│   └── controller/
│       └── HashtagController.java
│
├── busqueda/
│   ├── controller/
│   │   └── BusquedaController.java
│   ├── dto/...
│   └── service/...
│
├── perfil/
│   ├── controller/
│   │   ├── PerfilController.java
│   │   └── MiContenidoController.java
│   └── service/...
│
├── moderacion/
│   ├── controller/
│   │   └── ModeracionController.java
│   ├── entity/
│   │   ├── PostReporte.java
│   │   ├── EstadoReporte.java
│   │   ├── MotivoReporte.java
│   │   └── AccionModeracion.java
│   ├── dto/...
│   ├── repository/
│   │   └── PostReporteRepository.java
│   └── service/...
│
├── analitica/
│   ├── controller/
│   │   └── AnaliticaController.java
│   └── service/...
│
├── integracion/
│   ├── client/
│   │   ├── SocialUsuarioClientService.java
│   │   ├── SocialEquipoClientService.java
│   │   ├── ActividadClientService.java
│   │   ├── NoticiaClientService.java
│   │   └── NotificacionClientService.java
│   └── dto/
│       └── AutorSnapshotDto.java              # (interno, adaptador)
│
├── eventos/
│   └── listener/
│       └── NotificacionEventListener.java     # emite a notificaciones en AFTER_COMMIT
│
├── job/
│   └── PostVistaPurgaJob.java
│
└── seeder/
    └── SocialDataSeeder.java
```

---

