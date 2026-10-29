> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 10. API pública de `social` (para otros módulos)

```java
package upeu.edu.pe.Saludabemente.social.api;

public interface SocialApiService {
    void sincronizarPostDeActividad(ActividadSocialDto dto);
    void ocultarPostDeActividad(Long idActividad);
    void sincronizarPostDeNoticia(NoticiaSocialDto dto);
    void ocultarPostDeNoticia(Long idNoticia);
}
```

## 10.1 `ActividadSocialDto`

```java
public record ActividadSocialDto(
    Long idActividad,
    String nombre,
    String descripcion,
    String lugar,
    LocalDate fechaActividad,
    LocalTime horaInicio,
    LocalTime horaFin,
    String estado,
    String logoUrl,
    List<String> imagenesUrls,
    String nombreCoordinador,
    Long idUsuarioCreador,
    Long idEquipoActividad
) {}
```

## 10.2 `NoticiaSocialDto`

```java
public record NoticiaSocialDto(
    Long idNoticia,
    String titulo,
    String resumen,
    String imagenPortadaUrl,
    LocalDateTime fechaPublicacion,
    String estado,
    boolean oficial,
    Long idUsuarioAutor,
    Long idEquipoAutor,
    String urlCompleta
) {}
```

## 10.3 Implementación de sincronización (fragmento)

```java
@Override
@Transactional
public void sincronizarPostDeActividad(ActividadSocialDto dto) {
    Post post = postRepository
            .findByTipoContenidoAndRefId(TipoContenidoPost.ACTIVIDAD, dto.idActividad())
            .orElseGet(() -> crearPostBase(TipoContenidoPost.ACTIVIDAD, dto.idActividad()));

    post.setTitulo(dto.nombre());
    post.setResumen(dto.descripcion());
    post.setLugar(dto.lugar());
    post.setFechaEvento(dto.fechaActividad());
    post.setHoraInicio(dto.horaInicio() != null ? dto.horaInicio().toString() : null);
    post.setHoraFin(dto.horaFin() != null ? dto.horaFin().toString() : null);
    post.setRefEstado(dto.estado());
    post.setImagenUrl(dto.logoUrl());
    post.setCoordinadorNombre(dto.nombreCoordinador());
    post.setIdEquipoAutor(dto.idEquipoActividad());
    post.setAutorId(dto.idUsuarioCreador());
    post.setFechaActualizacion(LocalDateTime.now());
    post.setActivo(true);

    Post guardado = postRepository.save(post);

    imagenRepository.deleteByIdPost(guardado.getIdPost());
    if (dto.imagenesUrls() != null) {
        List<PostImagen> nuevas = new ArrayList<>();
        for (int i = 0; i < dto.imagenesUrls().size(); i++) {
            nuevas.add(new PostImagen(guardado.getIdPost(), dto.imagenesUrls().get(i), i + 1));
        }
        imagenRepository.saveAll(nuevas);
    }
}
```

`crearPostBase` asigna `tipoContenido`, `refId`, `fechaPublicacion=now()`, `activo=true`, `esOficial=false`, `alcanceTotal=0`, `sharesTotal=0`.

`sincronizarPostDeNoticia` es análogo. Además, si `dto.oficial() == true`, setea `esOficial=true` y crea un `PostFijado` automático con `scopeTipo=GLOBAL` y expiración configurable.

---


# 14. Eventos de dominio y notificaciones

## 14.1 Emisión

`social` emite eventos de dominio vía `ApplicationEventPublisher` en transacciones marcadas con `@Transactional`. La publicación efectiva se difiere con `@TransactionalEventListener(phase = AFTER_COMMIT)` para no notificar si la transacción hace rollback.

## 14.2 Eventos emitidos

| Evento | Se dispara en | Destinatario |
|---|---|---|
| `ComentarioEnPostEvent` | `ComentarioService.crear()` cuando `idComentarioPadre == null` | Autor del post |
| `RespuestaAComentarioEvent` | `ComentarioService.crear()` cuando `idComentarioPadre != null` | Autor del comentario padre |
| `MencionEvent` | `MencionService.procesar()` por cada mención a usuario | Usuario mencionado |
| `ReaccionEnPostEvent` | `ReaccionService.reaccionar()` en post | Autor del post |
| `NuevoPostDeEquipoEvent` | `PostService.crear()` o `ShareService.compartir()` cuando `idEquipoAutor != null` | Miembros del equipo (excepto el autor) |

## 14.3 Reglas

- Auto-notificación excluida (`idUsuarioAutor == idUsuarioDestinatario` → no se emite).
- Eventos se emiten una sola vez por acción.
- `NuevoPostDeEquipoEvent` requiere la lista de `idUsuariosDestinatarios`. Se resuelve vía `personal.api.social.SocialEquipoApi` en una enmienda futura (ver §21). En v1.0 de `social`, se emite con lista vacía hasta que `personal` exponga `miembrosActivosDeEquipo`.

## 14.4 Contrato con `notificaciones`

Pendiente de definir (Spring events vs RabbitMQ). `social` prepara los eventos en `social.api.eventos` con `@NamedInterface("social-api-eventos")` para que `notificaciones` los consuma por cualquiera de los dos mecanismos sin cambio en `social`.

---


# 15. Contratos con otros módulos

## 15.1 `seguridad` → `social` (APROBADO v1.2)

**Interface:** `upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi`
**`@NamedInterface`:** `seguridad-api-social`

```java
public interface SocialUsuarioApi {
    SocialUsuarioActualDto obtenerUsuarioActual();
    SocialUsuarioSnapshotDto obtenerPorId(Long idUsuario);
    Optional<SocialUsuarioSnapshotDto> obtenerPorUsername(String username);
    List<SocialUsuarioBusquedaDto> buscar(String query, int limite);
    boolean esAdministrador(Long idUsuario);
    List<SocialUsuarioSnapshotDto> obtenerPorIds(Collection<Long> ids);   // v1.2
}
```

DTOs:
- `SocialUsuarioActualDto(idUsuario, username, email, roles, esAdministrador)`
- `SocialUsuarioSnapshotDto(idUsuario, username, email, nombreCompleto, fotoUrl, roles, rolPrincipal, nombreEquipo, activo)`
- `SocialUsuarioBusquedaDto(idUsuario, username, nombreCompleto, fotoUrl)`

Decisiones del contrato:
- `username` corto, derivado de `nombres + apellidoPaterno`, no editable.
- `fotoUrl` siempre `null` en esta fase.
- `obtenerPorIds` capeado a 100 IDs por invocación.
- Live lookup puro (sin snapshot en `social`).

## 15.2 `personal` → `social` (PENDIENTE v1.0)

**Interface propuesta:** `SocialEquipoApi`
**`@NamedInterface`:** `personal-api-social`

```java
public interface SocialEquipoApi {
    SocialEquipoSnapshotDto obtenerPorId(Long idEquipo);
    List<SocialEquipoSnapshotDto> obtenerPorIds(Collection<Long> ids);
    List<SocialMiembroEquipoDto> equiposDeUsuario(Long idUsuario);
    boolean esMiembroDeEquipo(Long idUsuario, Long idEquipo);
    boolean esAdminDeEquipo(Long idUsuario, Long idEquipo);
    List<SocialEquipoBusquedaDto> buscar(String query, int limite);
}
```

DTOs propuestos:
- `SocialEquipoSnapshotDto(idEquipo, nombre, descripcion, logoUrl, activo, totalMiembros)`
- `SocialEquipoBusquedaDto(idEquipo, nombre, logoUrl)`
- `SocialMiembroEquipoDto(idUsuario, idEquipo, nombreEquipo, logoEquipoUrl, rolEnEquipo, activo)`

## 15.3 `actividades` → `social` (PENDIENTE)

**Dirección 1 (actividades → social):** `actividades` llama a `SocialApiService.sincronizarPostDeActividad(ActividadSocialDto)` en los siguientes puntos:
- `crearActividadIndividual`
- `OperacionServiceImpl.iniciar`
- `OperacionServiceImpl.finalizar`
- `OperacionServiceImpl.cancelar`
- `OperacionServiceImpl.reprogramar`
- `OperacionServiceImpl.transferirCoordinador`
- `ImagenServiceImpl.subirLogo`
- `ImagenServiceImpl.eliminarLogo`
- `ImagenServiceImpl.subirImagen`
- `ImagenServiceImpl.eliminarImagen`

**Dirección 2 (social → actividades):** `social` necesita de `actividades.api`:
- `Optional<ActividadSnapshotDto> obtenerActividadPorId(Long)`
- `List<ActividadBusquedaDto> buscarActividades(String q, int limite)`
- `List<ActividadSnapshotDto> actividadesPorEquipo(Long idEquipo)`

DTOs propuestos:
- `ActividadSnapshotDto(idActividad, nombre, estado, fechaActividad, logoUrl)`
- `ActividadBusquedaDto(idActividad, nombre, estado, fechaActividad)`

**Esta dirección rompe la regla original del handoff ("social no depende de actividades"). Aprobado: sí depende, vía `actividades.api`.**

## 15.4 `noticias` → `social` (PENDIENTE)

**Dirección 1 (noticias → social):** `noticias` llama a `SocialApiService.sincronizarPostDeNoticia(NoticiaSocialDto)` al publicar noticia oficial. Si `oficial=true`, `social` marca `ES_OFICIAL=true` y crea pin global automático.

**Dirección 2 (social → noticias):** `social` necesita de `noticias.api`:
- `Optional<NoticiaSnapshotDto> obtenerNoticiaPorId(Long)`

DTO propuesto:
- `NoticiaSnapshotDto(idNoticia, titulo, resumen, imagenPortadaUrl, fechaPublicacion, estado, oficial, urlCompleta)`

## 15.5 `notificaciones` → `social` (PENDIENTE)

Por definir el mecanismo (Spring events o RabbitMQ). `social` emite eventos en `social.api.eventos`.

## 15.6 `shared.storage` (EXISTENTE)

`social` usa:
- `AlmacenamientoImagenService.validar(MultipartFile)` → validación (mime, tamaño).
- `MinioService.guardar(MultipartFile, String prefijo)` → subida.

Prefijo reservado: `social/comentarios/{idPost}/`.

---


# 16. Integración MinIO

## 16.1 Lo que `social` NO sube

- Logo de actividad → ya en MinIO bajo `actividades/{id}/logo/...` (subido por `actividades`).
- Galería de actividad → ya en MinIO bajo `actividades/{id}/imagenes/...`.
- Imagen de portada de noticia → ya en MinIO bajo `noticias/{id}/portada/...`.
- Avatar de usuario → lo gestiona `seguridad`/`personal`.

`social` solo **referencia** las URLs existentes.

## 16.2 Lo que `social` sube

- Adjuntos de comentarios (`COMENTARIO_ADJUNTO.URL`): prefijo `social/comentarios/{idPost}/`.

## 16.3 Reglas

- Validación obligatoria con `AlmacenamientoImagenService.validar` antes de subir.
- Mime aceptado: `image/jpeg`, `image/png`, `image/webp`.
- Tamaño máximo: 5 MB por adjunto, 4 adjuntos por comentario (20 MB total).
- Al guardar un comentario con `adjuntosUrls`, `social` valida que cada URL empiece por `social/comentarios/{idPost}/`. URLs ajenas se rechazan con `BusinessRuleException`.

---

