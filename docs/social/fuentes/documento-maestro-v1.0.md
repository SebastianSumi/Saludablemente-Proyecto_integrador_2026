# Documento Maestro — Módulo `social`

**Versión:** 1.0
**Fecha:** 23/10/2026
**Estado:** Consolidado — incluye todas las decisiones cerradas en la fase de planificación
**Autor:** Responsable del módulo `social`
**Reemplaza:** Handoff original `# Handoff completo Módulo social.txt`

---

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

# 20. Fases de implementación

| Fase | Contenido | Dependencias externas | Duración estimada |
|---|---|---|---|
| **F0** | Esquema `SLB_SOCIAL`, `package-info.java`, `SocialModuleConfiguration` | — | 15 min |
| **F1** | `SocialApiService`, `ActividadSocialDto`, `NoticiaSocialDto`, `package-info` de `api` | — | 30 min |
| **F2** | Entidades base (`Post`, `PostImagen`, `Comentario`, `ReaccionPost`, `ReaccionComentario`, enums, IDs compuestas) + repositorios base | — | 2 h |
| **F3** | `PostServiceImpl` (sincronización desde actividades/noticias) | `actividades` (contrato DTO), `noticias` (contrato DTO) | 2 h |
| **F4** | `SocialUsuarioClientService`, `SocialEquipoClientService`, `ActividadClientService`, `NoticiaClientService` | Contratos aprobados (`seguridad` v1.2, `personal` v1.0, `actividades`, `noticias`) | 2 h |
| **F5** | `MuroServiceImpl` + `PostController` + `MuroController` (feed global, tabs, feed por equipo, feed por actividad, feed por hashtag, fijados) | — | 3 h |
| **F6** | `ComentarioServiceImpl` + `ComentarioController` (anidados 1 nivel, edición, adjuntos, cascada soft-delete) | `shared.storage` | 3 h |
| **F7** | `ReaccionServiceImpl` + `ReaccionController` (post y comentario, 7 tipos, ver quién reaccionó) | — | 2 h |
| **F8** | `HashtagServiceImpl` + `HashtagController` + `MencionServiceImpl` + `MencionController` (parseo, autocompletado, persistencia) | `seguridad`, `personal`, `actividades` (búsqueda) | 3 h |
| **F9** | `ShareServiceImpl`, `GuardadoServiceImpl`, `AlcanceServiceImpl` (C1) + `PostVistaPurgaJob` | — | 2 h |
| **F10** | `BusquedaServiceImpl` + `BusquedaController` | — | 1 h |
| **F11** | `PerfilActividadServiceImpl` + `PerfilController` + `MiContenidoController` | — | 1 h |
| **F12** | `ModeracionServiceImpl` + `ModeracionController` (reportes + panel + ocultar/restaurar) | — | 2 h |
| **F13** | `AnaliticaServiceImpl` + `AnaliticaController` | — | 2 h |
| **F14** | `NotificacionEventListener` + emisión de eventos + integración con `notificaciones` | Contrato `notificaciones` | 2 h |
| **F15** | Integración desde `actividades`: llamada a `sincronizarPostDeActividad` en los 10 puntos | Coordinación con `actividades` | 1 h |
| **F16** | Integración desde `noticias`: llamada a `sincronizarPostDeNoticia` al publicar oficial | Coordinación con `noticias` | 1 h |
| **F17** | `SocialDataSeeder` | — | 30 min |
| **F18** | Swagger, pruebas de integración, `ModularityTests` verde | — | 2 h |
| **Total** | | | **~32 h** |

---

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

# 22. Verificación final

## 22.1 Build

```bash
./mvnw clean compile
./mvnw test -Dtest=ModularityTests
./mvnw spring-boot:run
```

## 22.2 Flujo de prueba end-to-end

```http
# 1. Crear actividad (dispara sincronización)
POST /api/v1/agenda/actividades
{ "nombre": "Test Social", ..., "idUsuarioCreador": 1, "idUsuarioCoordinador": 1 }

# 2. Ver muro global
GET /api/v1/social/muro?tab=PARA_TI&page=0&size=10

# 3. Ver detalle
GET /api/v1/social/posts/1

# 4. Comentar (raíz)
POST /api/v1/social/posts/1/comentarios
{ "contenido": "@ana.lazaro excelente actividad #salud" }

# 5. Responder al comentario
POST /api/v1/social/posts/1/comentarios
{ "contenido": "Concuerdo", "idComentarioPadre": 1 }

# 6. Reaccionar al post
POST /api/v1/social/posts/1/reacciones
{ "tipo": "LOVE" }

# 7. Reaccionar al comentario
POST /api/v1/social/comentarios/1/reacciones
{ "tipo": "HAHA" }

# 8. Compartir
POST /api/v1/social/posts/1/share
{ "contenido": "Miren esto" }

# 9. Guardar
POST /api/v1/social/posts/1/guardar

# 10. Ver quién reaccionó
GET /api/v1/social/posts/1/reacciones/usuarios?tipo=LOVE&page=0

# 11. Crear post manual
POST /api/v1/social/posts
{ "titulo": "Anuncio", "resumen": "Hoy hay reunión", "idEquipoAutor": 3 }

# 12. Buscar
GET /api/v1/social/buscar?q=reunión&tipo=POST

# 13. Reportar
POST /api/v1/social/posts/1/reportar
{ "motivo": "SPAM", "detalle": "..." }

# 14. Moderar (como admin)
GET /api/v1/social/admin/reportes?estado=PENDIENTE
PATCH /api/v1/social/admin/reportes/1
{ "estado": "ACEPTADO" }

# 15. Finalizar actividad (dispara actualización del post)
PATCH /api/v1/operacion/1/finalizar

# 16. Verificar que el post muestra FINALIZADA y que el autor sigue enriquecido
GET /api/v1/social/posts/1
```

## 22.3 Swagger

Debe mostrar grupos:
- "Social - Muro"
- "Social - Posts"
- "Social - Comentarios"
- "Social - Reacciones"
- "Social - Búsqueda y Menciones"
- "Social - Perfil y Mi Contenido"
- "Social - Moderación (Admin)"
- "Social - Analítica (Admin)"

## 22.4 `ModularityTests` debe verificar

- `social` no importa `actividades.*` fuera de `actividades.api.*`.
- `social` no importa `noticias.*` fuera de `noticias.api.*`.
- `social` no importa `personal.*` fuera de `personal.api.social.*`.
- `social` no importa `seguridad.*` fuera de `seguridad.api.social.*`.
- `actividades` y `noticias` no importan `social.*` fuera de `social.api.*`.

---

**Fin del documento maestro v1.0.**

Este documento es la referencia única del módulo. Cualquier decisión que lo modifique debe registrarse como enmienda numerada y quedar reflejada en las secciones correspondientes.