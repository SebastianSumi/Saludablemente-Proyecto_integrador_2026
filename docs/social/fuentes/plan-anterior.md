# Plan de desarrollo — Módulo `social` (definitivo)

Arranco. Este documento consolida **todo el alcance cerrado** en una estructura de módulo implementable, sin código todavía. Primero el grafo Modulith, luego los contratos que necesitamos de otros módulos (DTOs + métodos), después el modelo de datos completo y la superficie de API, al final las fases.

---

## 1. Estructura del módulo y configuración Modulith

### 1.1 Grafo de dependencias definitivo

```
                       ┌─────────────────────┐
                       │   social.api        │◄── actividades (sincronizar)
                       │   (@NamedInterface) │◄── noticias    (sincronizar)
                       └─────────────────────┘

   social ──► seguridad.api   (identidad, autor enriquecido, @usuario, admin)
   social ──► personal.api    (equipos, admin de equipo, @equipo)
   social ──► actividades.api (snapshot, búsqueda, @actividad)
   social ──► noticias.api    (snapshot, búsqueda, @noticia)
   social ──► shared.storage  (adjuntos en comentarios)
   social ──► notificaciones.api (publicar eventos de dominio)
```

**Reglas duras:**
- `social` solo importa `*.api` (nunca internals de otros módulos).
- Otros módulos solo importan `social.api` (nunca internals de social).
- `social` publica eventos **solo hacia** `notificaciones`, no consume eventos.

### 1.2 `package-info.java` del módulo

```
src/main/java/upeu/edu/pe/Saludabemente/social/package-info.java

@ApplicationModule(
    displayName = "Social",
    allowedDependencies = {
        "seguridad-api", "seguridad-api-dto",
        "personal-api", "personal-api-dto",
        "actividades-api", "actividades-api-dto",
        "noticias-api", "noticias-api-dto",
        "notificaciones-api", "notificaciones-api-dto",
        "shared-storage"
    }
)
```

### 1.3 NamedInterfaces expuestas por `social`

```
social/api/package-info.java
    @NamedInterface("social-api")

social/api/dto/package-info.java
    @NamedInterface("social-api-dto")

social/api/eventos/package-info.java
    @NamedInterface("social-api-eventos")
```

---

## 2. Contratos que `social` NECESITA de otros módulos

Esta es la lista **exacta** de lo que hay que pedir/confirmar. Si algo no existe, la feature se degrada o se bloquea.

### 2.1 `seguridad.api` — lo que necesitamos

**DTOs:**

| DTO | Campos necesarios | Uso |
|---|---|---|
| `UsuarioSnapshotDto` | `idUsuario`, `username`, `nombreCompleto`, `email`, `fotoUrl`, `roles` (List<String>), `rolPrincipal`, `nombreEquipo` (o `List<EquipoResumen>`), `activo` | Snapshot de autor al crear posts/comentarios/reacciones. Autor enriquecido. |
| `EquipoResumenDto` (si no lo expone otro módulo) | `idEquipo`, `nombre` | Mostrar "Coordinador · Equipo Salud Mental" |
| `UsuarioBusquedaDto` | `idUsuario`, `username`, `nombreCompleto`, `fotoUrl` | Autocompletado `@usuario` |

**Métodos:**

| Método | Firma esperada | Uso |
|---|---|---|
| Obtener usuario | `UsuarioSnapshotDto obtenerUsuarioPorId(Long)` | Snapshot de autor |
| Buscar por username | `Optional<UsuarioSnapshotDto> obtenerUsuarioPorUsername(String)` | Resolver `@usuario` |
| Buscar por texto | `List<UsuarioBusquedaDto> buscarUsuarios(String q, int limit)` | Autocompletado `@usuario` |
| Usuario actual | `Long obtenerIdUsuarioActual()` o vía `@AuthenticationPrincipal` | Todo el módulo |
| Roles del actual | `Set<String> obtenerRolesActuales()` (o claim del JWT) | Verificación de admin |
| Es admin global | `boolean esAdministrador(Long idUsuario)` | Moderación, eliminar ajeno |

**Contrato de identidad (JWT/Keycloak):**
- `social` NO lee el JWT directamente. `seguridad` expone un bean que resuelve `idUsuario` y roles del request actual.
- `social` inyecta ese bean en controllers/services.
- Si `seguridad` prefiere un `@AuthenticationPrincipal` custom, `social` lo usa tal cual.

### 2.2 `personal.api` — lo que necesitamos

**DTOs:**

| DTO | Campos | Uso |
|---|---|---|
| `EquipoSnapshotDto` | `idEquipo`, `nombre`, `logoUrl`, `activo` | Publicar "como equipo", feed de equipo |
| `MiembroEquipoDto` | `idUsuario`, `idEquipo`, `rolEnEquipo` (ADMIN/MIEMBRO) | Validar permisos, "mis equipos" |

**Métodos:**

| Método | Uso |
|---|---|
| `Optional<EquipoSnapshotDto> obtenerEquipoPorId(Long)` | Snapshot al publicar como equipo |
| `List<EquipoSnapshotDto> equiposDeUsuario(Long idUsuario)` | Filtro "Mis equipos", dropdown "publicar como" |
| `boolean esAdminDeEquipo(Long idUsuario, Long idEquipo)` | Permiso para editar/eliminar post "como equipo" |
| `boolean esMiembroDeEquipo(Long idUsuario, Long idEquipo)` | Permiso para publicar como equipo |
| `List<EquipoBusquedaDto> buscarEquipos(String q, int limit)` | Autocompletado `@equipo` |

### 2.3 `actividades.api` — lo que necesitamos

**DTOs:**

| DTO | Campos | Uso |
|---|---|---|
| `ActividadSnapshotDto` | `idActividad`, `nombre`, `descripcion`, `lugar`, `fechaActividad`, `horaInicio`, `horaFin`, `estado`, `logoUrl`, `imagenesUrls`, `idUsuarioCoordinador`, `nombreCoordinador`, `idUsuarioCreador`, `nombreCreador`, `emailCreador`, `idEquipo` (nullable) | Snapshot de actividad |
| `ActividadBusquedaDto` | `idActividad`, `nombre`, `estado`, `fechaActividad` | Autocompletado `@actividad` |

**Métodos:**

| Método | Uso |
|---|---|
| `Optional<ActividadSnapshotDto> obtenerActividadPorId(Long)` | Snapshot al sincronizar y al resolver `@actividad` |
| `List<ActividadBusquedaDto> buscarActividades(String q, int limit)` | Autocompletado `@actividad` |
| `List<ActividadSnapshotDto> actividadesPorEquipo(Long idEquipo)` | Feed de equipo |

**Nota:** el `ActividadSocialDto` que `actividades` envía a `social` ya está definido en el handoff original. **Se mantiene igual** (es lo que viaja en el POST de sincronización), y el `ActividadSnapshotDto` de aquí es para consultas desde `social`.

### 2.4 `noticias.api` — lo que necesitamos

**DTOs:**

| DTO | Campos | Uso |
|---|---|---|
| `NoticiaSnapshotDto` | `idNoticia`, `titulo`, `resumen`, `imagenPortadaUrl`, `fechaPublicacion`, `estado` (PUBLICADA/BORRADOR), `oficial` (boolean), `idUsuarioAutor`, `nombreAutor`, `emailAutor`, `idEquipoAutor` (nullable), `urlCompleta` (para el link) | Snapshot de noticia |
| `NoticiaBusquedaDto` | `idNoticia`, `titulo`, `oficial` | Autocompletado `@noticia` (opcional, fase posterior) |

**Métodos:**

| Método | Uso |
|---|---|
| `Optional<NoticiaSnapshotDto> obtenerNoticiaPorId(Long)` | Snapshot al sincronizar |

**Contrato bidireccional:** `noticias` debe llamar a `social.api.sincronizarPostDeNoticia(NoticiaSocialDto)`. `social` define el DTO que `noticias` construye.

### 2.5 `notificaciones.api` — lo que necesitamos

**Métodos (o bus de eventos):**

| Método | Uso |
|---|---|
| `void publicar(NotificacionSocialEvent)` | `social` emite eventos de dominio |

O bien, `social` publica eventos vía Spring `ApplicationEventPublisher` y `notificaciones` los consume. **Recomiendo eventos de Spring** (más simple, coherente con Modulith). Definir:

- `ComentarioEnPostEvent` → autor del post notificado.
- `RespuestaAComentarioEvent` → autor del comentario padre notificado.
- `MencionEvent` → cada usuario mencionado notificado.
- `ReaccionEnPostEvent` → autor del post notificado.
- `NuevoPostDeEquipoEvent` → miembros del equipo notificados.

Cada evento lleva: `idPost`, `idComentario` (si aplica), `idUsuarioAutor` (quien dispara), `idUsuarioDestinatario`, `tipoEvento`, `deepLink`, `ocurridoEn`.

### 2.6 `shared.storage`

| Método | Uso |
|---|---|
| `String guardar(MultipartFile archivo, String prefijo)` | Subir adjuntos de comentarios |
| `void eliminar(String url)` | (Opcional) borrado físico de adjuntos |
| `AlmacenamientoImagenService.validar(MultipartFile)` | Validar antes de subir |

Prefijo reservado en MinIO: `social/comentarios/{idPost}/`.

---

## 3. Modelo de datos (`SLB_SOCIAL`)

### 3.1 Tablas

#### `POST`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST` | NUMBER PK | Identity |
| `TIPO_CONTENIDO` | VARCHAR2(20) NOT NULL | `ACTIVIDAD` / `NOTICIA` / `MANUAL` |
| `REF_ID` | NUMBER NULL | id de la actividad/noticia. NULL si manual |
| `ID_EQUIPO_AUTOR` | NUMBER NULL | Si se publica "como equipo" (manual o noticia oficial) |
| `TITULO` | VARCHAR2(150) NOT NULL | |
| `RESUMEN` | VARCHAR2(500) | |
| `LUGAR` | VARCHAR2(150) | |
| `FECHA_EVENTO` | DATE | |
| `HORA_INICIO` | VARCHAR2(8) | |
| `HORA_FIN` | VARCHAR2(8) | |
| `REF_ESTADO` | VARCHAR2(20) | `PROGRAMADA/EN_CURSO/FINALIZADA/CANCELADA/PUBLICADA` |
| `IMAGEN_URL` | VARCHAR2(500) | |
| `COORDINADOR_NOMBRE` | VARCHAR2(200) | |
| `AUTOR_ID` | NUMBER NOT NULL | |
| `AUTOR_NOMBRE` | VARCHAR2(200) NOT NULL | Snapshot |
| `AUTOR_EMAIL` | VARCHAR2(120) | Snapshot |
| `AUTOR_ROL` | VARCHAR2(50) | Snapshot del rol al publicar |
| `AUTOR_EQUIPO_NOMBRE` | VARCHAR2(100) | Snapshot del equipo al publicar |
| `AUTOR_FOTO_URL` | VARCHAR2(500) | Snapshot |
| `FECHA_PUBLICACION` | TIMESTAMP NOT NULL | |
| `FECHA_ACTUALIZACION` | TIMESTAMP | |
| `ACTIVO` | NUMBER(1) NOT NULL DEFAULT 1 | |
| `ES_OFICIAL` | NUMBER(1) NOT NULL DEFAULT 0 | Solo noticias oficiales |
| `ALCANCE_TOTAL` | NUMBER NOT NULL DEFAULT 0 | C1 |
| `SHARES_TOTAL` | NUMBER NOT NULL DEFAULT 0 | Contador de shares |
| `ORIGEN_POST_ID` | NUMBER NULL | Si es un share: id del post original |

**Índices:**
- `UK_POST_TIPO_REF (TIPO_CONTENIDO, REF_ID)` → idempotencia.
- `IX_POST_MURO (ACTIVO, FECHA_PUBLICACION DESC)`.
- `IX_POST_EQUIPO (ID_EQUIPO_AUTOR, ACTIVO, FECHA_PUBLICACION DESC)`.
- `IX_POST_OFICIAL (ES_OFICIAL, FECHA_PUBLICACION DESC)`.

#### `POST_IMAGEN`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST_IMAGEN` | NUMBER PK | |
| `ID_POST` | NUMBER NOT NULL | |
| `URL` | VARCHAR2(500) NOT NULL | |
| `ORDEN` | NUMBER | |

#### `POST_HASHTAG` (para hashtags de posts)
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST_HASHTAG` | NUMBER PK | |
| `ID_POST` | NUMBER NOT NULL | |
| `TAG` | VARCHAR2(50) NOT NULL | Lowercase |

**Índice:** `IX_POST_HASHTAG_TAG (TAG, ID_POST)`.

#### `POST_VISTA` (C1)
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST_VISTA` | NUMBER PK | |
| `ID_POST` | NUMBER NOT NULL | |
| `ID_USUARIO` | NUMBER NOT NULL | |
| `FECHA_PRIMERA_VISTA` | TIMESTAMP NOT NULL | |
| `FECHA_ULTIMA_VISTA` | TIMESTAMP NOT NULL | |

**Índice único:** `UK_POST_VISTA (ID_POST, ID_USUARIO)`.
**Índice:** `IX_POST_VISTA_RECIENTES (ID_POST, FECHA_ULTIMA_VISTA DESC)`.
**Purga:** job mensual, borra filas con `FECHA_ULTIMA_VISTA < NOW - 180 days` (solo afecta al desglose temporal; `ALCANCE_TOTAL` ya está persistido).

#### `POST_GUARDADO`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST` | NUMBER | PK compuesta |
| `ID_USUARIO` | NUMBER | PK compuesta |
| `FECHA_GUARDADO` | TIMESTAMP NOT NULL | |

#### `POST_FIJADO`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST_FIJADO` | NUMBER PK | |
| `ID_POST` | NUMBER NOT NULL | |
| `SCOPE_TIPO` | VARCHAR2(20) | `GLOBAL` / `EQUIPO` / `ACTIVIDAD` |
| `SCOPE_ID` | NUMBER NULL | id de equipo o actividad. NULL para GLOBAL |
| `FECHA_FIJADO` | TIMESTAMP NOT NULL | |
| `ID_USUARIO_FIJADOR` | NUMBER NOT NULL | |

**Índice único:** `UK_POST_FIJADO (ID_POST, SCOPE_TIPO, SCOPE_ID)`.
**Índice:** `IX_POST_FIJADO_SCOPE (SCOPE_TIPO, SCOPE_ID, FECHA_FIJADO DESC)`.

#### `POST_REPORTE` (moderación mínima)
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST_REPORTE` | NUMBER PK | |
| `ID_POST` | NUMBER NULL | Reporte de post |
| `ID_COMENTARIO` | NUMBER NULL | Reporte de comentario (uno de los dos) |
| `ID_USUARIO_REPORTA` | NUMBER NOT NULL | |
| `MOTIVO` | VARCHAR2(50) NOT NULL | `SPAM/OFENSIVO/ACOSO/FALSO/OTRO` |
| `DETALLE` | VARCHAR2(500) | |
| `FECHA_REPORTE` | TIMESTAMP NOT NULL | |
| `ESTADO` | VARCHAR2(20) NOT NULL DEFAULT 'PENDIENTE' | `PENDIENTE/REVISADO/ACEPTADO/RECHAZADO` |
| `ID_USUARIO_REVISOR` | NUMBER NULL | |
| `FECHA_REVISION` | TIMESTAMP NULL | |
| `ACCION_TOMADA` | VARCHAR2(50) | `OCULTADO/NINGUNA` |

**Índice:** `IX_POST_REPORTE_ESTADO (ESTADO, FECHA_REPORTE DESC)`.

#### `COMENTARIO`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_COMENTARIO` | NUMBER PK | |
| `ID_POST` | NUMBER NOT NULL | |
| `ID_COMENTARIO_PADRE` | NUMBER NULL | 1 nivel |
| `ID_USUARIO_AUTOR` | NUMBER NOT NULL | |
| `AUTOR_NOMBRE` | VARCHAR2(200) NOT NULL | Snapshot |
| `AUTOR_EMAIL` | VARCHAR2(120) | |
| `AUTOR_FOTO_URL` | VARCHAR2(500) | |
| `AUTOR_ROL` | VARCHAR2(50) | |
| `AUTOR_EQUIPO_NOMBRE` | VARCHAR2(100) | |
| `CONTENIDO` | VARCHAR2(1000) NOT NULL | |
| `EDITADO` | NUMBER(1) NOT NULL DEFAULT 0 | |
| `FECHA_EDICION` | TIMESTAMP NULL | |
| `FECHA_COMENTARIO` | TIMESTAMP NOT NULL | |
| `ACTIVO` | NUMBER(1) NOT NULL DEFAULT 1 | Soft-delete |

**Índices:** `IX_COMENTARIO_POST (ID_POST, ID_COMENTARIO_PADRE, FECHA_COMENTARIO ASC)`.

#### `COMENTARIO_ADJUNTO`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_COMENTARIO_ADJUNTO` | NUMBER PK | |
| `ID_COMENTARIO` | NUMBER NOT NULL | |
| `URL` | VARCHAR2(500) NOT NULL | |
| `ORDEN` | NUMBER | |
| `MIME` | VARCHAR2(50) | |
| `SIZE_BYTES` | NUMBER | |

#### `COMENTARIO_HASHTAG`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_COMENTARIO_HASHTAG` | NUMBER PK | |
| `ID_COMENTARIO` | NUMBER NOT NULL | |
| `TAG` | VARCHAR2(50) NOT NULL | |

**Índice:** `IX_COMENTARIO_HASHTAG_TAG (TAG, ID_COMENTARIO)`.

#### `MENCION` (unificada para posts y comentarios)
| Columna | Tipo | Notas |
|---|---|---|
| `ID_MENCION` | NUMBER PK | |
| `TIPO_CONTENIDO` | VARCHAR2(20) NOT NULL | `POST` / `COMENTARIO` |
| `ID_CONTENIDO` | NUMBER NOT NULL | id del post o comentario |
| `TIPO_MENCION` | VARCHAR2(20) NOT NULL | `USUARIO` / `ACTIVIDAD` / `EQUIPO` |
| `REF_ID` | NUMBER NOT NULL | id del usuario/actividad/equipo |
| `OFFSET_INICIO` | NUMBER | Para el frontend |
| `OFFSET_FIN` | NUMBER | |

**Índice:** `IX_MENCION_CONTENIDO (TIPO_CONTENIDO, ID_CONTENIDO)`.
**Índice:** `IX_MENCION_USUARIO (TIPO_MENCION, REF_ID)` → "dónde me mencionan".

#### `REACCION`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_POST` | NUMBER | PK compuesta |
| `ID_COMENTARIO` | NUMBER NULL | Reacción a comentario (nueva feature) |
| `ID_USUARIO` | NUMBER | PK compuesta |
| `TIPO` | VARCHAR2(20) NOT NULL | LIKE / LOVE / HAHA / WOW / SAD / APLAUSO / ENOJADO |
| `FECHA_REACCION` | TIMESTAMP NOT NULL | |

**Nota crítica:** la PK compuesta original era `(ID_POST, ID_USUARIO)`. Con reacciones a comentarios, hay dos casos:
- Reacción a post: `(ID_POST, NULL, ID_USUARIO)`.
- Reacción a comentario: `(NULL, ID_COMENTARIO, ID_USUARIO)`.

**Recomendación:** separar en dos entidades distintas `ReaccionPost` y `ReaccionComentario` para no complicar la PK. Alternativa: PK compuesta `(ID_POST, ID_COMENTARIO, ID_USUARIO)` con constraint `CHECK (uno de los dos es NULL)`. **Recomiendo dos entidades separadas** — más limpio en JPA.

- `REACCION_POST (ID_POST, ID_USUARIO, TIPO, FECHA_REACCION)` PK compuesta.
- `REACCION_COMENTARIO (ID_COMENTARIO, ID_USUARIO, TIPO, FECHA_REACCION)` PK compuesta.

#### `SHARE`
| Columna | Tipo | Notas |
|---|---|---|
| `ID_SHARE` | NUMBER PK | |
| `ID_POST_ORIGINAL` | NUMBER NOT NULL | |
| `ID_USUARIO` | NUMBER NOT NULL | |
| `ID_EQUIPO_AUTOR` | NUMBER NULL | Si comparte como equipo |
| `CONTENIDO` | VARCHAR2(500) | Texto opcional al compartir |
| `FECHA_SHARE` | TIMESTAMP NOT NULL | |

**Nota:** cada share genera un **nuevo `POST`** con `ORIGEN_POST_ID = idPostOriginal` y `TIPO_CONTENIDO='SHARE'`. La tabla `SHARE` registra el evento; el post con `ORIGEN_POST_ID` es lo que se ve en el muro. `POST.SHARES_TOTAL` del original se incrementa.

### 3.2 Resumen de tablas

| Tabla | Feature |
|---|---|
| `POST` | Core |
| `POST_IMAGEN` | Galería |
| `POST_HASHTAG` | Hashtags |
| `POST_VISTA` | Alcance (C1) |
| `POST_GUARDADO` | Guardar/favoritos |
| `POST_FIJADO` | Fijados |
| `POST_REPORTE` | Moderación |
| `COMENTARIO` | Core + anidados + edición |
| `COMENTARIO_ADJUNTO` | Fotos en comentarios |
| `COMENTARIO_HASHTAG` | Hashtags |
| `MENCION` | Menciones |
| `REACCION_POST` | Reacciones a posts |
| `REACCION_COMENTARIO` | Reacciones a comentarios |
| `SHARE` | Compartir |

---

## 4. Superficie de API (`social.api` + controllers REST)

### 4.1 API pública del módulo (para otros módulos)

```java
public interface SocialApiService {
    void sincronizarPostDeActividad(ActividadSocialDto dto);
    void ocultarPostDeActividad(Long idActividad);
    void sincronizarPostDeNoticia(NoticiaSocialDto dto);
    void ocultarPostDeNoticia(Long idNoticia);
}
```

**DTOs en `social.api.dto`:**
- `ActividadSocialDto` (ya definido en handoff).
- `NoticiaSocialDto` (nuevo):
  ```
  Long idNoticia
  String titulo
  String resumen
  String imagenPortadaUrl
  LocalDateTime fechaPublicacion
  String estado
  boolean oficial
  Long idUsuarioAutor
  String nombreAutor
  String emailAutor
  Long idEquipoAutor
  String urlCompleta
  ```

**DTOs de eventos en `social.api.eventos`:**
- `ComentarioEnPostEvent`
- `RespuestaAComentarioEvent`
- `MencionEvent`
- `ReaccionEnPostEvent`
- `NuevoPostDeEquipoEvent`

### 4.2 Controllers REST (endpoints)

**Muro / Feed**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/muro?tab=&page=&size=` | Muro global con scope |
| GET | `/api/v1/social/muro/equipos/{idEquipo}` | Muro de equipo |
| GET | `/api/v1/social/muro/actividades/{idActividad}` | Muro de actividad |
| GET | `/api/v1/social/hashtags/{tag}/muro` | Muro de hashtag |

**Posts**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/posts/{idPost}` | Detalle (registra vista, C1) |
| POST | `/api/v1/social/posts` | Crear post manual (personal o equipo) |
| PATCH | `/api/v1/social/posts/{idPost}` | Editar post propio |
| DELETE | `/api/v1/social/posts/{idPost}` | Eliminar post propio (soft) |
| POST | `/api/v1/social/posts/{idPost}/share` | Compartir |
| POST | `/api/v1/social/posts/{idPost}/guardar` | Guardar |
| DELETE | `/api/v1/social/posts/{idPost}/guardar` | Quitar guardado |
| POST | `/api/v1/social/posts/{idPost}/fijar` | Fijar (admin/coord) |
| DELETE | `/api/v1/social/posts/{idPost}/fijar` | Desfijar |
| POST | `/api/v1/social/posts/{idPost}/reportar` | Reportar |

**Comentarios**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/posts/{idPost}/comentarios?page=&size=` | Listar (anidado) |
| POST | `/api/v1/social/posts/{idPost}/comentarios` | Crear (raíz o respuesta) |
| PATCH | `/api/v1/social/posts/{idPost}/comentarios/{idComentario}` | Editar |
| DELETE | `/api/v1/social/posts/{idPost}/comentarios/{idComentario}` | Eliminar (soft) |
| POST | `/api/v1/social/posts/{idPost}/comentarios/adjuntos` | Subir adjunto |
| POST | `/api/v1/social/posts/{idPost}/comentarios/{idComentario}/reportar` | Reportar |

**Reacciones**
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/social/posts/{idPost}/reacciones` | Reaccionar/actualizar |
| DELETE | `/api/v1/social/posts/{idPost}/reacciones` | Quitar |
| GET | `/api/v1/social/posts/{idPost}/reacciones/usuarios?tipo=&page=` | Ver quién reaccionó |
| POST | `/api/v1/social/comentarios/{idComentario}/reacciones` | Reaccionar a comentario |
| DELETE | `/api/v1/social/comentarios/{idComentario}/reacciones` | Quitar |

**Búsqueda**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/buscar?q=&tipo=post\|autor\|hashtag&page=` | Búsqueda unificada |

**Menciones**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/menciones/buscar?tipo=usuario\|actividad\|equipo&q=` | Autocompletado |

**Mi contenido**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/mi-contenido?tab=publicaciones\|comentarios\|reacciones\|guardados&page=` | Vista de autor |
| GET | `/api/v1/social/usuarios/{idUsuario}/actividad?tab=` | Perfil público de actividad |

**Moderación (admin)**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/admin/reportes?estado=PENDIENTE&page=` | Cola de reportes |
| PATCH | `/api/v1/social/admin/reportes/{idReporte}` | Resolver (aceptar/rechazar) |
| PATCH | `/api/v1/social/admin/posts/{idPost}/ocultar` | Ocultar post |
| PATCH | `/api/v1/social/admin/posts/{idPost}/restaurar` | Restaurar post |

**Analítica (admin)**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/social/admin/metricas?desde=&hasta=` | Panel agregado |

---

## 5. Servicios internos

| Servicio | Responsabilidad |
|---|---|
| `PostServiceImpl` (implements `SocialApiService` + `PostService`) | Sincronización, CRUD posts, share, guardado, fijado, alcance |
| `ComentarioServiceImpl` | CRUD comentarios, adjuntos, anidación, edición |
| `ReaccionServiceImpl` | Reacciones a post y a comentario |
| `MuroServiceImpl` | Feed global, por equipo, por actividad, por hashtag, fijados primero |
| `HashtagServiceImpl` | Parser de hashtags, trending |
| `MencionServiceImpl` | Parser, resolución, autocompletado |
| `BusquedaServiceImpl` | Búsqueda unificada |
| `PerfilActividadServiceImpl` | "Mi contenido" y perfil público |
| `ModeracionServiceImpl` | Reportes, ocultar/restaurar, historial |
| `AnaliticaServiceImpl` | Métricas agregadas |
| `ShareServiceImpl` | Crear share → nuevo post, incrementar contador |
| `AlcanceServiceImpl` | Registrar vista (C1), job de purga |
| `SeguridadClientService` | Capa anticorrupción → `seguridad.api` |
| `PersonalClientService` | Capa anticorrupción → `personal.api` |
| `ActividadClientService` | Capa anticorrupción → `actividades.api` |
| `NoticiaClientService` | Capa anticorrupción → `noticias.api` |
| `NotificacionClientService` | Publicar eventos → `notificaciones` |
| `NotificacionEventListener` | Emite eventos en AFTER_COMMIT |

**Recomendación:** cada `*ClientService` es un wrapper mínimo que adapta `*.api` al DTO interno del módulo. Así, si `seguridad.api` cambia, solo se ajusta el wrapper.

---

## 6. Fases de implementación (orden revisado)

| Fase | Contenido | Dependencias externas |
|---|---|---|
| **F0** | Esquema `SLB_SOCIAL`, `package-info` | Ninguna |
| **F1** | `SocialApiService`, `ActividadSocialDto`, `NoticiaSocialDto`, `package-info` de `api` | Ninguna |
| **F2** | Entidades base (`Post`, `PostImagen`, `Comentario`, `ReaccionPost`, `ReaccionComentario`, enums) + repositorios | Ninguna |
| **F3** | `PostServiceImpl` (sincronización desde actividades/noticias) + `sincronizarPostDeNoticia` | `seguridad.api`, `actividades.api` (creación del DTO) |
| **F4** | `SeguridadClientService`, `PersonalClientService`, `ActividadClientService`, `NoticiaClientService` | `seguridad.api`, `personal.api`, `actividades.api`, `noticias.api` |
| **F5** | `MuroServiceImpl` + `PostController` + `MuroController` (feed global, filtros, fijados) | — |
| **F6** | `ComentarioServiceImpl` + `ComentarioController` (anidados, edición, adjuntos) | `shared.storage` |
| **F7** | `ReaccionServiceImpl` + `ReaccionController` (post y comentario) | — |
| **F8** | `HashtagServiceImpl`, `MencionServiceImpl` (parseo, autocompletado) | `seguridad.api`, `actividades.api`, `personal.api` |
| **F9** | `ShareServiceImpl`, `GuardadoService`, `AlcanceServiceImpl` (C1) | — |
| **F10** | `BusquedaServiceImpl` + `BusquedaController` | — |
| **F11** | `PerfilActividadServiceImpl` + `MiContenidoController` + `PerfilController` | — |
| **F12** | `ModeracionServiceImpl` + `ModeracionController` (reportes + panel) | `seguridad.api` (admin) |
| **F13** | `AnaliticaServiceImpl` + `AnaliticaController` | — |
| **F14** | `NotificacionClientService` + eventos + integración con `notificaciones` | `notificaciones.api` |
| **F15** | Integración desde `actividades` (llamada a `sincronizarPostDeActividad`) | Coordinar con dueño de `actividades` |
| **F16** | Integración desde `noticias` (llamada a `sincronizarPostDeNoticia`) | Coordinar con dueño de `noticias` |
| **F17** | `SocialDataSeeder` (opcional) | — |
| **F18** | Swagger + pruebas + `ModularityTests` en verde | — |

---

## 7. Lo que necesito coordinar antes de F3

| # | Coordinar con | Qué pedir |
|---|---|---|
| 1 | `seguridad` | DTOs + métodos listados en §2.1 (incluyendo `buscarPorUsername`, `esAdministrador`) |
| 2 | `personal` | DTOs + métodos listados en §2.2 |
| 3 | `actividades` | DTOs + métodos listados en §2.3 + que llamen a `sincronizarPostDeActividad` en sus puntos |
| 4 | `noticias` | DTOs + métodos listados en §2.4 + que llamen a `sincronizarPostDeNoticia` al publicar noticia oficial |
| 5 | `notificaciones` | Confirmar bus de eventos (Spring events vs RabbitMQ) y contrato de `NotificacionSocialEvent` |

---

## 8. Próximo paso natural

Con esto cerrado, el siguiente paso lógico es **generar los DTOs y métodos de contrato** que `social` necesita de cada módulo (lo que mencionaste: "luego lo generaremos en los contratos los DTO que necesitamos, los métodos que necesitamos"). Eso es material de `*.api` de cada módulo dueño, no de `social`.

¿Arrancamos con eso (los contratos a pedir a `seguridad`, `personal`, `actividades`, `noticias`, `notificaciones`), o prefieres primero consolidar este plan en un único documento Markdown tipo "handoff v2" para que el equipo lo firme?