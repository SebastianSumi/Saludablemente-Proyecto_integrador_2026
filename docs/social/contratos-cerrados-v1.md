# Contratos Social — versión de implementación 1

Fecha: 23/10/2026. Autorización: cerrar contratos primero y aplicar las reglas
de oficialidad/administrador de equipo confirmadas por el usuario. Ver E03 en
[enmiendas.md](enmiendas.md). Este archivo describe código real nuevo; el texto
histórico del maestro se conserva en contratos.md. No acredita F3/F14/F15/F16.

## 1. Identidad y autenticación

Se conservan sin renombrar los seis métodos de SocialUsuarioApi v1.2 y sus DTOs.
NamedInterface: seguridad-api-social, dentro de seguridad/api/social.

Extensión complementaria:

```java
interface SocialIdentidadApi {
    Optional<SocialIdentidadDto> obtenerIdentidadActiva(Long idUsuario);
    List<SocialIdentidadDto> obtenerPorPersonas(Collection<Long> idsPersona);
}
record SocialIdentidadDto(Long idUsuario, Long idPersona, String email, List<String> roles) {}
```

Reglas: cuenta activa con vínculo persona real; ausencia/inactividad → empty.
Lote máximo 100 personas distintas, sin null; exceso → BusinessRuleException/409,
sin truncamiento. Omite personas sin cuenta activa; no presupone orden.
Roles inmutables. El estado activo de la persona para membresía se consulta en Personal.

SocialUsuarioApi continúa rechazando inactivos con un subtipo público de
ResourceNotFoundException: UsuarioSocialInactivoException. Su HTTP global sigue 404.
SocialUsuarioClientService traduce solo ese subtipo a BusinessRuleException/409
para RN34; no traduce ausencia de JWT ni de usuario como si fueran inactividad.
El controlador futuro resolverá una sola vez el autenticado por request.

El adaptador autores(Collection<Long>) deduplica, divide en lotes de hasta 100,
indexa por idUsuario y completa ausentes con [Usuario eliminado], sin persistirlos.
No accede a Keycloak, entidades ni repositorios de Seguridad.

## 2. Equipos y membresías

Contrato SocialEquipoApi en personal/api/social; NamedInterface personal-api-social:

```java
SocialEquipoSnapshotDto obtenerPorId(Long idEquipo);
List<SocialEquipoSnapshotDto> obtenerPorIds(List<Long> ids);
SocialEquipoPerfilDto obtenerPerfil(Long idEquipo);
List<SocialEquipoPerfilDto> obtenerPerfilesPorIds(List<Long> ids);
List<SocialMiembroEquipoDto> equiposDeUsuario(Long idUsuario);
boolean esMiembroDeEquipo(Long idUsuario, Long idEquipo);
boolean esAdminDeEquipo(Long idUsuario, Long idEquipo);
List<SocialEquipoBusquedaDto> buscar(String query, int limite);
List<Long> miembrosActivosDeEquipo(Long idEquipo);
```

DTOs: Snapshot(idEquipo,nombre,descripcion,logoUrl,activo,totalMiembros),
Busqueda(idEquipo,nombre,logoUrl),
Miembro(idUsuario,idEquipo,nombreEquipo,logoEquipoUrl,rolEnEquipo,activo).

- obtenerPorId ausente → 404; batches vacíos → lista vacía; máximo 100 IDs únicos
  y omisión de ausentes; exceso → 409. Las lecturas de snapshot pueden incluir
  equipo inactivo con activo=false, sin conferir permisos.
- Persona.equipo es la fuente real de membresía. Se usa idPersona resuelto en
  Seguridad; nunca nombres de equipo del snapshot ni igualdad de IDs.
- Administrador de equipo: cuenta activa con rol exacto Coordinador, persona activa
  y equipo activo al que pertenece. ADMIN/MIEMBRO son valores del DTO rolEnEquipo,
  no roles nuevos de Seguridad. Admin global se autoriza por separado en Social.
- El modelo existente admite cero o un equipo por persona; no se fabrica membresía N:M.
- Buscar vacío → []; límite efectivo 1–20; solo equipos activos y orden nombre/ID.
- miembrosActivosDeEquipo devuelve IDs de cuentas activas cuyas personas están
  activas en ese equipo activo, en lotes internos de hasta 100 personas.
- totalMiembros cuenta personas activas, incluidas las que no tienen cuenta.
- Actualización E04: logoUrl/logoEquipoUrl usan imagenUrl de TeamPerfilDTO, sin
  cambiar TeamSnapshotDTO ni re-subir imágenes. El perfil visual completo se consulta
  con obtenerPerfil/obtenerPerfilesPorIds, en lotes máximos de 100 equipos.

```java
record SocialEquipoPerfilDto(Long idEquipo, String nombre, String descripcion,
    String imagenUrl, String color, String slogan, String referenciaBiblica,
    String textoVersiculo, Long idUsuarioLider, SocialLiderEquipoDto lider,
    boolean activo, long totalMiembros) {}
record SocialLiderEquipoDto(Long idUsuario, String username, String nombreCompleto,
    String fotoUrl, boolean activo) {}
```

El líder se resuelve en vivo por SocialUsuarioApi, deduplicando IDs e indexando
el lote por idUsuario. Sin líder: lider=null. Cuenta ausente/inactiva: placeholder
[Usuario eliminado], con ID conservado y activo=false. No se exponen correo/roles.
El perfil se usa para mostrar identidad del equipo; no agrega permisos por ser líder.
Fixture del contrato, sin endpoint REST supuesto: [perfil-equipo-social.json](perfil-equipo-social.json).

Implementación compuesta: seguridad/api/impl/SocialEquipoApiImpl. Usa APIs públicas
de Personal/Teams e identidad de Seguridad. No importa repositorios/entidades externos.
Personal expone PersonaEquipoSocialApi para consultar equipo, personas activas y
conteo por idPersona/idEquipo desde su propio repositorio. Esta composición evita
una nueva dependencia inversa Personal→Seguridad.

Los cambios de entidad/CRUD Teams añadieron líder/imagen y cambiaron su esquema;
no fueron implementados ni sobrescritos por Social. E04 extiende únicamente su API
de perfiles por lotes para consumir esos datos. idUsuarioLider no reemplaza
silenciosamente la regla de Coordinador que el usuario autorizó.

## 3. Actividades

NamedInterface actividades-api-social; API dedicada, sin modificar el snapshot previo:

```java
interface SocialActividadApi {
    Optional<ActividadSnapshotDto> obtenerActividadPorId(Long idActividad);
    List<ActividadBusquedaDto> buscarActividades(String query, int limite);
    List<ActividadSnapshotDto> actividadesPorEquipo(Long idEquipo);
    Optional<ActividadPublicacionDto> obtenerPublicacion(Long idActividad);
}
```

- ActividadSnapshotDto dedicado: idActividad,nombre,estado,fechaActividad,logoUrl.
  Su paquete api/social evita colisión con api/dto/ActividadSnapshotDto de Asistencias.
- ActividadBusquedaDto: idActividad,nombre,estado,fechaActividad. Consulta vacía → [];
  límite 1–20; orden fecha/ID descendente; CANCELADA no se excluye.
- ActividadPublicacionDto: los 13 campos del payload de sincronización de actividad,
  incluida galería ordenada, coordinador opcional, creador e idEquipoActividad.
  El módulo dueño no importa Social para construir este DTO.
- Ausente/null ID → Optional.empty. No se altera obtenerSnapshot anterior que usa null.
- ID_EQUIPO_ACTIVIDAD es atribución explícita nullable. Datos antiguos sin atribución
  devuelven null y no pertenecen al filtro de equipo. Asignar ese dato en el flujo
  de Actividades queda para F15: no se infiere por el creador ni por el coordinador.
- Imágenes son referencias existentes, sin re-subida. Coordinador ausente/inactivo
  deja nombreCoordinador=null; otros errores se propagan.

La conversión a ActividadSocialDto y las diez llamadas directas desde productores
se implementarán en F15; no están conectadas por esta entrega de contratos.

## 4. Noticias

NamedInterface noticias-api-social:

```java
interface SocialNoticiaApi {
    Optional<NoticiaSnapshotDto> obtenerNoticiaPorId(Long idNoticia);
}
record NoticiaSnapshotDto(Long idNoticia, String titulo, String resumen,
    String imagenPortadaUrl, LocalDateTime fechaPublicacion, String estado,
    boolean oficial, String urlCompleta, Long idUsuarioAutor, Long idEquipoAutor) {}
```

- Solo PUBLICADO en Noticias se expone, normalizado a PUBLICADA para Social.
  BORRADOR/ARCHIVADO/ausente/null ID → Optional.empty.
- Oficialidad explícita: false al crear por defecto. Campo oficial en request,
  entidad y respuesta; la actualización de borrador lo conserva desde el request.
- resumen es un campo propio opcional de hasta 500 caracteres: no se transforma
  subtítulo, contenido HTML ni categoría en resumen/oficialidad.
- idEquipoAutor opcional es autoría, diferente de idsTeams destinatarios. Si se
  informa, Noticias verifica que exista y esté activo mediante TeamApiService.
- urlCompleta es opcional y explícita (máximo 500), sin inventar una ruta frontend.
- Nuevos campos se mapean por el mapper y el servicio existentes. Los DTOs del feed
  público previo no cambian. Las migraciones de columnas deben revisarse antes de BD real.

La publicación/sincronización/ocultamiento hacia Social continúa pendiente de F16.

## 5. Almacenamiento de comentarios

Contrato compartido aislado en shared/storage/social. shared sigue OPEN; no se
declara una NamedInterface ficticia shared-storage.

```java
interface AdjuntoSocialStorageApi {
    void validar(MultipartFile archivo);
    AdjuntoSocialDto guardar(Long idPost, MultipartFile archivo);
    void validarReferencias(Long idPost, List<String> urls);
}
record AdjuntoSocialDto(String objectKey, String url, String mime, long sizeBytes) {}
```

- 5 MiB = 5*1024*1024 bytes, máximo cuatro referencias únicas por comentario.
- Solo image/jpeg, image/png, image/webp, no vacío; extensión si existe restringida
  a jpg/jpeg/png/webp. No sustituye análisis antivirus ni verifica firma binaria.
- guardar usa exclusivamente social/comentarios/{idPost}/ y retorna clave y URL separadas.
- adjuntosUrls usa URL pública canónica del servidor; clave suelta no es una URL válida.
  Se rechaza otra autoridad/post, traversal, query, fragmento y codificación ambigua.
- Validar pertenencia de URL no acredita existencia del objeto ni autoría del upload;
  el servicio de comentario deberá verificar lo necesario al implementar F6.
- No cambia upload genérico, MIME de otros módulos ni multipart global 15MB/20MB.
  No borra objetos automáticamente al eliminar un comentario.

El endpoint multipart y la emisión de referencias desde comentarios son F6, no esta API.

## 6. Eventos y puente de notificaciones

Cinco records del maestro en social/api/eventos, NamedInterface social-api-eventos:
ComentarioEnPostEvent, RespuestaAComentarioEvent, MencionEvent,
ReaccionEnPostEvent y NuevoPostDeEquipoEvent. Los enums de payload TipoReaccion y
TipoContenidoMencion viven en social/api/dto para no exponer tipos internos de entidades.
La lista de destinatarios del evento de equipo es una copia inmutable.

Puente con NamedInterface notificaciones-api-social:

```java
interface SocialNotificacionApi { void enviar(SocialAvisoDto aviso); }
record SocialAvisoDto(UUID idEvento, TipoAvisoSocial tipo, Long idPersonaDestinatario,
    Long idPost, Long idComentario, Long idEquipo) {}
```

TipoAvisoSocial: SOCIAL_COMENTARIO_POST, SOCIAL_RESPUESTA_COMENTARIO,
SOCIAL_MENCION, SOCIAL_REACCION_POST, SOCIAL_NUEVO_POST_EQUIPO.

El adaptador valida identificadores/código y delega en NotificacionApi.enviar:
origenModulo=social, referencia=idPost, emailDestino=null. Variables: idEvento
como UUID string, idPost y, cuando correspondan, idComentario/idEquipo numéricos.
No consume ni publica directamente NotificacionMensaje desde Social.
Contrato de las cinco plantillas: [plantillas-social.json](plantillas-social.json).

Responsabilidad del listener futuro de Social (F14): AFTER_COMMIT, excluir autoavisos,
resolver idPersona por SocialIdentidadApi, resolver post de mención a comentario,
generar/reutilizar identidad de aviso según política de entrega y resolver miembros.
El puente propaga fallos: no confirma entrega y no ofrece exactly-once.
No se han provisionado plantillas, conectado listeners ni demostrado RabbitMQ/reintentos.
Rutas de pantalla/deep links y deduplicación durable requieren cierre funcional de F14.

## 7. Validación y límites de esta entrega

Pruebas aisladas en src/test/java/.../socialcontracts; ejecutar desde SaludabementeERP:

```powershell
.\mvnw.cmd -o -Pcontratos-social test
```

El perfil recompila fuentes principales y únicamente pruebas de contrato desde cero
en target/contratos-social; no inicia Spring, Keycloak, MinIO, RabbitMQ ni Oracle.
No modifica el perfil dev ni oculta fallos globales: es una suite acotada expresamente.
Incluye deserialización real de J01/J02, mapeo de oficialidad, identidad/membresías,
límites de lote, fallback, adjuntos, bridge de avisos y límites API-only de Social.
J03–J19 de CRUD no son endpoints implementados por esta entrega y no se declaran aprobados.

El estado y resultados exactos se registran en estado.md. F0/Oracle, grafo modular
global y comportamiento transaccional siguen pendientes. La existencia de contratos
compilados permite implementar los adaptadores siguientes; no declara terminado Social.
