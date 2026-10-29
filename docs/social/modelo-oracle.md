> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

# 6. Modelo de base de datos completo

## 6.1 Script de creación del esquema

```sql
-- Ejecutar como SYSTEM en FREEPDB1
CREATE USER SLB_SOCIAL IDENTIFIED BY "123456"
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW,
      CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE
    TO SLB_SOCIAL;

GRANT UNLIMITED TABLESPACE TO SLB_SOCIAL;
```

`ddl-auto: update` crea las tablas automáticamente desde las entidades JPA.

## 6.2 Tablas

### 6.2.1 `POST`

| Columna | Tipo | Null | Default | Descripción |
|---|---|---|---|---|
| `ID_POST` | NUMBER | NO | identity | PK |
| `TIPO_CONTENIDO` | VARCHAR2(20) | NO | — | `ACTIVIDAD` / `NOTICIA` / `MANUAL` / `SHARE` |
| `REF_ID` | NUMBER | SÍ | — | id de actividad o noticia. NULL si MANUAL o SHARE |
| `ORIGEN_POST_ID` | NUMBER | SÍ | — | id del post original si SHARE. NULL en otros casos |
| `ID_EQUIPO_AUTOR` | NUMBER | SÍ | — | id de equipo si publicado "como equipo" |
| `TITULO` | VARCHAR2(150) | NO | — | |
| `RESUMEN` | VARCHAR2(500) | SÍ | — | |
| `LUGAR` | VARCHAR2(150) | SÍ | — | |
| `FECHA_EVENTO` | DATE | SÍ | — | |
| `HORA_INICIO` | VARCHAR2(8) | SÍ | — | |
| `HORA_FIN` | VARCHAR2(8) | SÍ | — | |
| `REF_ESTADO` | VARCHAR2(20) | SÍ | — | `PROGRAMADA` / `EN_CURSO` / `FINALIZADA` / `CANCELADA` / `PUBLICADA` |
| `IMAGEN_URL` | VARCHAR2(500) | SÍ | — | logo/portada |
| `COORDINADOR_NOMBRE` | VARCHAR2(200) | SÍ | — | snapshot del coordinador de la actividad |
| `AUTOR_ID` | NUMBER | NO | — | id usuario autor |
| `FECHA_PUBLICACION` | TIMESTAMP | NO | — | |
| `FECHA_ACTUALIZACION` | TIMESTAMP | SÍ | — | |
| `ACTIVO` | NUMBER(1) | NO | 1 | soft delete |
| `ES_OFICIAL` | NUMBER(1) | NO | 0 | solo noticias oficiales |
| `ALCANCE_TOTAL` | NUMBER | NO | 0 | vistas únicas (C1) |
| `SHARES_TOTAL` | NUMBER | NO | 0 | contador de shares |

**Constraints:**
- `PK_POST (ID_POST)`
- `UK_POST_TIPO_REF (TIPO_CONTENIDO, REF_ID)` — idempotencia
- `UK_POST_AUTOR_ORIGEN (AUTOR_ID, ORIGEN_POST_ID)` — un share por usuario por post
- `CHECK_POST_TIPO (TIPO_CONTENIDO IN ('ACTIVIDAD','NOTICIA','MANUAL','SHARE'))`
- `CHECK_POST_REF (TIPO_CONTENIDO IN ('ACTIVIDAD','NOTICIA') AND REF_ID IS NOT NULL) OR (TIPO_CONTENIDO IN ('MANUAL','SHARE') AND REF_ID IS NULL)`
- `CHECK_POST_ORIGEN (TIPO_CONTENIDO = 'SHARE' AND ORIGEN_POST_ID IS NOT NULL) OR (TIPO_CONTENIDO <> 'SHARE' AND ORIGEN_POST_ID IS NULL)`

**Índices:**
- `IX_POST_MURO (ACTIVO, FECHA_PUBLICACION DESC)`
- `IX_POST_EQUIPO (ID_EQUIPO_AUTOR, ACTIVO, FECHA_PUBLICACION DESC)`
- `IX_POST_OFICIAL (ES_OFICIAL, FECHA_PUBLICACION DESC)`
- `IX_POST_AUTOR (AUTOR_ID, FECHA_PUBLICACION DESC)`
- `IX_POST_ORIGEN (ORIGEN_POST_ID)`

### 6.2.2 `POST_IMAGEN`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST_IMAGEN` | NUMBER | NO | PK |
| `ID_POST` | NUMBER | NO | FK lógica |
| `URL` | VARCHAR2(500) | NO | |
| `ORDEN` | NUMBER | SÍ | |

**Índices:** `IX_POST_IMAGEN_POST (ID_POST, ORDEN)`.

### 6.2.3 `POST_HASHTAG`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST_HASHTAG` | NUMBER | NO | PK |
| `ID_POST` | NUMBER | NO | |
| `TAG` | VARCHAR2(50) | NO | lowercase |

**Índices:** `IX_POST_HASHTAG_TAG (TAG, ID_POST)`.

### 6.2.4 `POST_VISTA` (C1)

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST_VISTA` | NUMBER | NO | PK |
| `ID_POST` | NUMBER | NO | |
| `ID_USUARIO` | NUMBER | NO | |
| `FECHA_PRIMERA_VISTA` | TIMESTAMP | NO | |
| `FECHA_ULTIMA_VISTA` | TIMESTAMP | NO | |

**Constraints:**
- `UK_POST_VISTA (ID_POST, ID_USUARIO)`

**Índices:** `IX_POST_VISTA_RECIENTES (ID_POST, FECHA_ULTIMA_VISTA DESC)`.

**Purga:** mensual, `DELETE WHERE FECHA_ULTIMA_VISTA < NOW - 180d`.

### 6.2.5 `POST_GUARDADO`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST` | NUMBER | NO | PK compuesta |
| `ID_USUARIO` | NUMBER | NO | PK compuesta |
| `FECHA_GUARDADO` | TIMESTAMP | NO | |

**Constraints:** `PK_POST_GUARDADO (ID_POST, ID_USUARIO)`.

### 6.2.6 `POST_FIJADO`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST_FIJADO` | NUMBER | NO | PK |
| `ID_POST` | NUMBER | NO | |
| `SCOPE_TIPO` | VARCHAR2(20) | NO | `GLOBAL` / `EQUIPO` / `ACTIVIDAD` |
| `SCOPE_ID` | NUMBER | SÍ | id de equipo o actividad. NULL para GLOBAL |
| `FECHA_FIJADO` | TIMESTAMP | NO | |
| `ID_USUARIO_FIJADOR` | NUMBER | NO | |
| `FECHA_EXPIRACION` | TIMESTAMP | SÍ | opcional |

**Constraints:**
- `UK_POST_FIJADO (ID_POST, SCOPE_TIPO, SCOPE_ID)`
- `CHECK_SCOPE (SCOPE_TIPO='GLOBAL' AND SCOPE_ID IS NULL) OR (SCOPE_TIPO IN ('EQUIPO','ACTIVIDAD') AND SCOPE_ID IS NOT NULL)`

**Índices:** `IX_POST_FIJADO_SCOPE (SCOPE_TIPO, SCOPE_ID, FECHA_FIJADO DESC)`.

### 6.2.7 `POST_REPORTE`

| Columna | Tipo | Null | Default | Descripción |
|---|---|---|---|---|
| `ID_POST_REPORTE` | NUMBER | NO | — | PK |
| `ID_POST` | NUMBER | SÍ | — | reporte de post |
| `ID_COMENTARIO` | NUMBER | SÍ | — | reporte de comentario |
| `ID_USUARIO_REPORTA` | NUMBER | NO | — | |
| `MOTIVO` | VARCHAR2(50) | NO | — | `SPAM` / `OFENSIVO` / `ACOSO` / `FALSO` / `OTRO` |
| `DETALLE` | VARCHAR2(500) | SÍ | — | |
| `FECHA_REPORTE` | TIMESTAMP | NO | — | |
| `ESTADO` | VARCHAR2(20) | NO | PENDIENTE | `PENDIENTE` / `REVISADO` / `ACEPTADO` / `RECHAZADO` |
| `ID_USUARIO_REVISOR` | NUMBER | SÍ | — | |
| `FECHA_REVISION` | TIMESTAMP | SÍ | — | |
| `ACCION_TOMADA` | VARCHAR2(50) | SÍ | — | `OCULTADO` / `NINGUNA` |

**Constraints:**
- `CHECK_REPORTE_TARGET (ID_POST IS NOT NULL AND ID_COMENTARIO IS NULL) OR (ID_POST IS NULL AND ID_COMENTARIO IS NOT NULL)`
- `UK_REPORTE_UNICO`: único por (ID_USUARIO_REPORTA + target). Se maneja en service con `BusinessRuleException` si ya existe.

**Índices:** `IX_POST_REPORTE_ESTADO (ESTADO, FECHA_REPORTE DESC)`.

### 6.2.8 `COMENTARIO`

| Columna | Tipo | Null | Default | Descripción |
|---|---|---|---|---|
| `ID_COMENTARIO` | NUMBER | NO | — | PK |
| `ID_POST` | NUMBER | NO | — | |
| `ID_COMENTARIO_PADRE` | NUMBER | SÍ | — | 1 nivel |
| `ID_USUARIO_AUTOR` | NUMBER | NO | — | |
| `CONTENIDO` | VARCHAR2(1000) | NO | — | |
| `EDITADO` | NUMBER(1) | NO | 0 | |
| `FECHA_EDICION` | TIMESTAMP | SÍ | — | |
| `FECHA_COMENTARIO` | TIMESTAMP | NO | — | |
| `ACTIVO` | NUMBER(1) | NO | 1 | soft delete |

**Índices:**
- `IX_COMENTARIO_POST (ID_POST, ID_COMENTARIO_PADRE, FECHA_COMENTARIO ASC)`
- `IX_COMENTARIO_AUTOR (ID_USUARIO_AUTOR, FECHA_COMENTARIO DESC)`

**Regla:** `ID_COMENTARIO_PADRE` solo apunta a comentarios con `ID_COMENTARIO_PADRE = NULL` (validado en service).

### 6.2.9 `COMENTARIO_ADJUNTO`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_COMENTARIO_ADJUNTO` | NUMBER | NO | PK |
| `ID_COMENTARIO` | NUMBER | NO | |
| `URL` | VARCHAR2(500) | NO | prefijo `social/comentarios/{idPost}/` |
| `ORDEN` | NUMBER | SÍ | |
| `MIME` | VARCHAR2(50) | SÍ | |
| `SIZE_BYTES` | NUMBER | SÍ | |

**Índices:** `IX_COMENTARIO_ADJUNTO (ID_COMENTARIO, ORDEN)`.

### 6.2.10 `COMENTARIO_HASHTAG`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_COMENTARIO_HASHTAG` | NUMBER | NO | PK |
| `ID_COMENTARIO` | NUMBER | NO | |
| `TAG` | VARCHAR2(50) | NO | |

**Índices:** `IX_COMENTARIO_HASHTAG_TAG (TAG, ID_COMENTARIO)`.

### 6.2.11 `MENCION`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_MENCION` | NUMBER | NO | PK |
| `TIPO_CONTENIDO` | VARCHAR2(20) | NO | `POST` / `COMENTARIO` |
| `ID_CONTENIDO` | NUMBER | NO | id del post o comentario |
| `TIPO_MENCION` | VARCHAR2(20) | NO | `USUARIO` / `ACTIVIDAD` / `EQUIPO` |
| `REF_ID` | NUMBER | NO | idUsuario / idActividad / idEquipo |
| `OFFSET_INICIO` | NUMBER | SÍ | posición en el texto |
| `OFFSET_FIN` | NUMBER | SÍ | |

**Constraints:** `CHECK_MENCION_TIPO (TIPO_MENCION IN ('USUARIO','ACTIVIDAD','EQUIPO'))`.

**Índices:**
- `IX_MENCION_CONTENIDO (TIPO_CONTENIDO, ID_CONTENIDO)`
- `IX_MENCION_USUARIO (TIPO_MENCION, REF_ID)` — "dónde me mencionan"

### 6.2.12 `REACCION_POST`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_POST` | NUMBER | NO | PK compuesta |
| `ID_USUARIO` | NUMBER | NO | PK compuesta |
| `TIPO` | VARCHAR2(20) | NO | LIKE / LOVE / HAHA / WOW / SAD / APLAUSO / ENOJADO |
| `FECHA_REACCION` | TIMESTAMP | NO | |

**Constraints:**
- `PK_REACCION_POST (ID_POST, ID_USUARIO)`
- `CHECK_REACCION_TIPO (TIPO IN ('LIKE','LOVE','HAHA','WOW','SAD','APLAUSO','ENOJADO'))`

**Índices:** `IX_REACCION_POST_TIPO (ID_POST, TIPO)`.

### 6.2.13 `REACCION_COMENTARIO`

| Columna | Tipo | Null | Descripción |
|---|---|---|---|
| `ID_COMENTARIO` | NUMBER | NO | PK compuesta |
| `ID_USUARIO` | NUMBER | NO | PK compuesta |
| `TIPO` | VARCHAR2(20) | NO | mismo enum |
| `FECHA_REACCION` | TIMESTAMP | NO | |

**Constraints:** `PK_REACCION_COMENTARIO (ID_COMENTARIO, ID_USUARIO)`.

**Índices:** `IX_REACCION_COMENTARIO_TIPO (ID_COMENTARIO, TIPO)`.

## 6.3 Enums

```java
public enum TipoContenidoPost {
    ACTIVIDAD, NOTICIA, MANUAL, SHARE
}

public enum TipoReaccion {
    LIKE, LOVE, HAHA, WOW, SAD, APLAUSO, ENOJADO
}

public enum TipoMencion {
    USUARIO, ACTIVIDAD, EQUIPO
}

public enum TipoContenidoMencion {
    POST, COMENTARIO
}

public enum ScopeFijado {
    GLOBAL, EQUIPO, ACTIVIDAD
}

public enum EstadoReporte {
    PENDIENTE, REVISADO, ACEPTADO, RECHAZADO
}

public enum MotivoReporte {
    SPAM, OFENSIVO, ACOSO, FALSO, OTRO
}

public enum AccionModeracion {
    OCULTADO, NINGUNA
}

public enum TabMuro {
    PARA_TI, RECIENTES, MIS_EQUIPOS, SOLO_ACTIVIDADES, SOLO_NOTICIAS,
    MIS_PUBLICACIONES, MIS_GUARDADOS
}

public enum TabMiContenido {
    PUBLICACIONES, COMENTARIOS, REACCIONES, GUARDADOS
}
```

## 6.4 Diagrama de relaciones

```
POST (1) ──── (N) POST_IMAGEN
POST (1) ──── (N) POST_HASHTAG
POST (1) ──── (N) POST_VISTA          [C1]
POST (1) ──── (N) POST_GUARDADO
POST (1) ──── (N) POST_FIJADO         [por scope]
POST (1) ──── (N) POST_REPORTE
POST (1) ──── (N) COMENTARIO
POST (1) ──── (N) REACCION_POST
POST (1) ──── (N) POST [como SHARE]   [ORIGEN_POST_ID]

COMENTARIO (1) ──── (N) COMENTARIO    [ID_COMENTARIO_PADRE]
COMENTARIO (1) ──── (N) COMENTARIO_ADJUNTO
COMENTARIO (1) ──── (N) COMENTARIO_HASHTAG
COMENTARIO (1) ──── (N) REACCION_COMENTARIO
COMENTARIO (1) ──── (N) POST_REPORTE

(MENCION) ──► POST / COMENTARIO      [TIPO_CONTENIDO + ID_CONTENIDO]
```

---


# 7. Entidades JPA

Todas declaran `@Table(name="X", schema="SLB_SOCIAL")`.

## 7.1 `Post`

```java
@Entity
@Table(name = "POST", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST")
    private Long idPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 20)
    private TipoContenidoPost tipoContenido;

    @Column(name = "REF_ID")
    private Long refId;

    @Column(name = "ORIGEN_POST_ID")
    private Long origenPostId;

    @Column(name = "ID_EQUIPO_AUTOR")
    private Long idEquipoAutor;

    @Column(name = "TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "RESUMEN", length = 500)
    private String resumen;

    @Column(name = "LUGAR", length = 150)
    private String lugar;

    @Column(name = "FECHA_EVENTO")
    private LocalDate fechaEvento;

    @Column(name = "HORA_INICIO", length = 8)
    private String horaInicio;

    @Column(name = "HORA_FIN", length = 8)
    private String horaFin;

    @Column(name = "REF_ESTADO", length = 20)
    private String refEstado;

    @Column(name = "IMAGEN_URL", length = 500)
    private String imagenUrl;

    @Column(name = "COORDINADOR_NOMBRE", length = 200)
    private String coordinadorNombre;

    @Column(name = "AUTOR_ID", nullable = false)
    private Long autorId;

    @Column(name = "FECHA_PUBLICACION", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "FECHA_ACTUALIZACION")
    private LocalDateTime fechaActualizacion;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "ES_OFICIAL", nullable = false)
    private Boolean esOficial = false;

    @Column(name = "ALCANCE_TOTAL", nullable = false)
    private Long alcanceTotal = 0L;

    @Column(name = "SHARES_TOTAL", nullable = false)
    private Long sharesTotal = 0L;
}
```

## 7.2 `PostImagen`

```java
@Entity
@Table(name = "POST_IMAGEN", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostImagen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST_IMAGEN") private Long idPostImagen;
    @Column(name = "ID_POST", nullable = false) private Long idPost;
    @Column(name = "URL", nullable = false, length = 500) private String url;
    @Column(name = "ORDEN") private Integer orden;

    public PostImagen(Long idPost, String url, Integer orden) {
        this.idPost = idPost;
        this.url = url;
        this.orden = orden;
    }
}
```

## 7.3 `PostHashtag`

```java
@Entity
@Table(name = "POST_HASHTAG", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostHashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST_HASHTAG") private Long id;
    @Column(name = "ID_POST", nullable = false) private Long idPost;
    @Column(name = "TAG", nullable = false, length = 50) private String tag;
}
```

## 7.4 `PostVista`

```java
@Entity
@Table(name = "POST_VISTA", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostVista {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST_VISTA") private Long id;
    @Column(name = "ID_POST", nullable = false) private Long idPost;
    @Column(name = "ID_USUARIO", nullable = false) private Long idUsuario;
    @Column(name = "FECHA_PRIMERA_VISTA", nullable = false) private LocalDateTime fechaPrimeraVista;
    @Column(name = "FECHA_ULTIMA_VISTA", nullable = false) private LocalDateTime fechaUltimaVista;
}
```

## 7.5 `PostGuardado`

```java
@Entity
@Table(name = "POST_GUARDADO", schema = "SLB_SOCIAL")
@IdClass(PostGuardadoId.class)
@Getter @Setter @NoArgsConstructor
public class PostGuardado {
    @Id @Column(name = "ID_POST") private Long idPost;
    @Id @Column(name = "ID_USUARIO") private Long idUsuario;
    @Column(name = "FECHA_GUARDADO", nullable = false) private LocalDateTime fechaGuardado;
}
```

## 7.6 `PostFijado`

```java
@Entity
@Table(name = "POST_FIJADO", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostFijado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST_FIJADO") private Long id;
    @Column(name = "ID_POST", nullable = false) private Long idPost;
    @Enumerated(EnumType.STRING)
    @Column(name = "SCOPE_TIPO", nullable = false, length = 20) private ScopeFijado scopeTipo;
    @Column(name = "SCOPE_ID") private Long scopeId;
    @Column(name = "FECHA_FIJADO", nullable = false) private LocalDateTime fechaFijado;
    @Column(name = "ID_USUARIO_FIJADOR", nullable = false) private Long idUsuarioFijador;
    @Column(name = "FECHA_EXPIRACION") private LocalDateTime fechaExpiracion;
}
```

## 7.7 `PostReporte`

```java
@Entity
@Table(name = "POST_REPORTE", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostReporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POST_REPORTE") private Long id;
    @Column(name = "ID_POST") private Long idPost;
    @Column(name = "ID_COMENTARIO") private Long idComentario;
    @Column(name = "ID_USUARIO_REPORTA", nullable = false) private Long idUsuarioReporta;
    @Enumerated(EnumType.STRING)
    @Column(name = "MOTIVO", nullable = false, length = 50) private MotivoReporte motivo;
    @Column(name = "DETALLE", length = 500) private String detalle;
    @Column(name = "FECHA_REPORTE", nullable = false) private LocalDateTime fechaReporte;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20) private EstadoReporte estado = EstadoReporte.PENDIENTE;
    @Column(name = "ID_USUARIO_REVISOR") private Long idUsuarioRevisor;
    @Column(name = "FECHA_REVISION") private LocalDateTime fechaRevision;
    @Enumerated(EnumType.STRING)
    @Column(name = "ACCION_TOMADA", length = 50) private AccionModeracion accionTomada;
}
```

## 7.8 `Comentario`

```java
@Entity
@Table(name = "COMENTARIO", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class Comentario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMENTARIO") private Long idComentario;
    @Column(name = "ID_POST", nullable = false) private Long idPost;
    @Column(name = "ID_COMENTARIO_PADRE") private Long idComentarioPadre;
    @Column(name = "ID_USUARIO_AUTOR", nullable = false) private Long idUsuarioAutor;
    @Column(name = "CONTENIDO", nullable = false, length = 1000) private String contenido;
    @Column(name = "EDITADO", nullable = false) private Boolean editado = false;
    @Column(name = "FECHA_EDICION") private LocalDateTime fechaEdicion;
    @Column(name = "FECHA_COMENTARIO", nullable = false) private LocalDateTime fechaComentario;
    @Column(name = "ACTIVO", nullable = false) private Boolean activo = true;
}
```

## 7.9 `ComentarioAdjunto`

```java
@Entity
@Table(name = "COMENTARIO_ADJUNTO", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class ComentarioAdjunto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMENTARIO_ADJUNTO") private Long id;
    @Column(name = "ID_COMENTARIO", nullable = false) private Long idComentario;
    @Column(name = "URL", nullable = false, length = 500) private String url;
    @Column(name = "ORDEN") private Integer orden;
    @Column(name = "MIME", length = 50) private String mime;
    @Column(name = "SIZE_BYTES") private Long sizeBytes;
}
```

## 7.10 `ComentarioHashtag`

```java
@Entity
@Table(name = "COMENTARIO_HASHTAG", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class ComentarioHashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMENTARIO_HASHTAG") private Long id;
    @Column(name = "ID_COMENTARIO", nullable = false) private Long idComentario;
    @Column(name = "TAG", nullable = false, length = 50) private String tag;
}
```

## 7.11 `Mencion`

```java
@Entity
@Table(name = "MENCION", schema = "SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class Mencion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MENCION") private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTENIDO", nullable = false, length = 20) private TipoContenidoMencion tipoContenido;
    @Column(name = "ID_CONTENIDO", nullable = false) private Long idContenido;
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_MENCION", nullable = false, length = 20) private TipoMencion tipoMencion;
    @Column(name = "REF_ID", nullable = false) private Long refId;
    @Column(name = "OFFSET_INICIO") private Integer offsetInicio;
    @Column(name = "OFFSET_FIN") private Integer offsetFin;
}
```

## 7.12 `ReaccionPost` y `ReaccionPostId`

```java
@Entity
@Table(name = "REACCION_POST", schema = "SLB_SOCIAL")
@IdClass(ReaccionPostId.class)
@Getter @Setter @NoArgsConstructor
public class ReaccionPost {
    @Id @Column(name = "ID_POST") private Long idPost;
    @Id @Column(name = "ID_USUARIO") private Long idUsuario;
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20) private TipoReaccion tipo;
    @Column(name = "FECHA_REACCION", nullable = false) private LocalDateTime fechaReaccion;
}

public class ReaccionPostId implements Serializable {
    private Long idPost;
    private Long idUsuario;
    // equals, hashCode, constructor vacío
}
```

## 7.13 `ReaccionComentario` y `ReaccionComentarioId`

```java
@Entity
@Table(name = "REACCION_COMENTARIO", schema = "SLB_SOCIAL")
@IdClass(ReaccionComentarioId.class)
@Getter @Setter @NoArgsConstructor
public class ReaccionComentario {
    @Id @Column(name = "ID_COMENTARIO") private Long idComentario;
    @Id @Column(name = "ID_USUARIO") private Long idUsuario;
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20) private TipoReaccion tipo;
    @Column(name = "FECHA_REACCION", nullable = false) private LocalDateTime fechaReaccion;
}

public class ReaccionComentarioId implements Serializable {
    private Long idComentario;
    private Long idUsuario;
    // equals, hashCode, constructor vacío
}
```

---


# 17. Configuración

## 17.1 `application-dev.yml` (fragmento)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: SLB_PERSONAL
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 20MB

saludablemente:
  social:
    muro:
      page-size-default: 20
      page-size-max: 50
    comentarios:
      max-length: 1000
      max-adjuntos: 4
      max-adjunto-size-mb: 5
    reacciones:
      tipos: [LIKE, LOVE, HAHA, WOW, SAD, APLAUSO, ENOJADO]
    fijados:
      max-por-scope: 3
    alcance:
      purga-dias: 180
      contar-auto-vista: false
      contar-admin-vista: false
    menciones:
      regex-usuario: "@([a-z0-9\\.]+)"
      limite-autocomplete: 5
    hashtags:
      regex: "#([a-z0-9_áéíóúñ]{2,50})"
    noticias-oficiales:
      auto-pin-dias: 7
```

## 17.2 Job de purga

```java
@Component
@RequiredArgsConstructor
public class PostVistaPurgaJob {

    private final PostVistaRepository repository;
    @Value("${saludablemente.social.alcance.purga-dias}") private int purgaDias;

    @Scheduled(cron = "0 0 3 1 * *")  // mensual, día 1 a las 03:00
    public void purgar() {
        LocalDateTime corte = LocalDateTime.now().minusDays(purgaDias);
        repository.purgarAntiguas(corte);
    }
}
```

---


# 18. Seeders

## 18.1 `SocialDataSeeder` (`@Profile("dev")`, `@Order(40)`)

- Si `PostRepository.count() > 0` → omitir.
- Si `ActividadesDataSeeder` ya ejecutó y llamó `sincronizarPostDeActividad`, no se requiere trabajo adicional.
- Opcional: crear 1 comentario y 1 reacción de ejemplo sobre el primer post para validar el render del detalle.

---

