# Contratos reales frente al objetivo

**Actualización de implementación:** ver [contratos-cerrados-v1.md](contratos-cerrados-v1.md)
y el último apartado de estado.md. Se han añadido APIs dedicadas, membresías con IDs
reales, oficialidad y adaptadores de almacenamiento/avisos. El inventario siguiente
conserva la inspección inicial y sus brechas históricas; no describe esos archivos nuevos.

Base Java: SaludabementeERP/src/main/java/upeu/edu/pe/Saludabemente.
Las firmas del maestro están completas en contratos.md; no son stubs instalados.

## Seguridad: existente, inspección estática

`seguridad/api/social/SocialUsuarioApi.java` expone:

```java
SocialUsuarioActualDto obtenerUsuarioActual();
SocialUsuarioSnapshotDto obtenerPorId(Long idUsuario);
Optional<SocialUsuarioSnapshotDto> obtenerPorUsername(String username);
List<SocialUsuarioBusquedaDto> buscar(String query, int limite);
boolean esAdministrador(Long idUsuario);
List<SocialUsuarioSnapshotDto> obtenerPorIds(Collection<Long> ids);
```

DTO actual: idUsuario, username, email, List<String> roles, boolean esAdministrador.
Snapshot: idUsuario, username, email, nombreCompleto, fotoUrl, roles, rolPrincipal, nombreEquipo, Boolean activo.
La API documenta búsqueda capeada a 20; batch trunca a primeros 100, omite inexistentes/inactivos y no conserva orden. Agrupar IDs en lotes e indexar por idUsuario al enriquecer el feed. obtenerPorId rechaza inactivos con 404; RN-34 exige 409 para acciones: cerrar adaptación sin cambiar seguridad global.

## Personal y equipos: pendiente

No existe personal.api.social.SocialEquipoApi en el árbol revisado. Existe teams.api.TeamApiService. Confirmar con los dueños dónde vivirá la membresía, cómo se relacionan idUsuario e idPersona y cómo se determina admin de equipo. No importar sus repositorios ni asumir que validarEquipos prueba membresía. No inventar miembrosActivosDeEquipo: el maestro registra esa enmienda como pendiente.

## Actividades: API existente diferente

`ActividadApiService` ofrece `ActividadSnapshotDto obtenerSnapshot(Long idActividad)` (null si no existe), listarEnCurso(), listarPorRango(LocalDate, LocalDate), obtenerSnapshots(List<Long>), listarFinalizadasNoCerradas(), listarInscritos(Long), estaInscrito(Long,Long), marcarCierreRealizado(Long).

El snapshot real incluye idActividad, nombre, descripcion, estado, fechaActividad, horaInicio/horaFin (LocalTime), lugar, latitudOficial, longitudOficial, toleranciaGeocercaMetros, requiereCheckInOut, duracionMinutos, periodo, idUsuarioCreador, permiteAutoservicio y permiteEscaneoCoordinador. No incluye logoUrl. No expone buscarActividades ni actividadesPorEquipo. No sustituir Optional por null sin adaptación explícita. Los diez puntos de sincronización que pide el maestro son un plan de integración F15, no evidencia de integración existente.

## Noticias: API existente diferente

`NoticiaQueryApi`: obtenerUltimasPublicadas(int), obtenerPublicadaPorId(Long) → Optional<NoticiaFeedPublicDTO>, contarPublicadasPorCategoria(Long).
DTO público: id, titulo, subtitulo, nombreCategoria, colorCategoria, imagenUrl, fechaPublicacion. No aporta oficial, autor, equipo, estado ni urlCompleta necesarios para NoticiaSocialDto. Requiere acuerdo; no mapear subtitulo a resumen por suposición.

## Notificaciones: API existente, puente social pendiente

`NotificacionApi.enviar(Long idPersona, String codigoEvento, String origenModulo, Long idOrigenReferencia, Map<String,Object> variables, String emailDestino)`.
`NotificacionMensaje` se documenta como payload interno de RabbitMQ; no consumirlo desde social aunque resida en api. Los eventos de dominio del maestro no son equivalentes a esta llamada. Definir códigos, plantillas, deep links, identidad destinataria, entrega tras commit y reintentos antes de F14. La existencia de RabbitMQ interno no cierra la decisión del puente social.

## Almacenamiento: existente con semántica distinta

AlmacenamientoImagenService.guardar(MultipartFile,String,long maxBytes) valida vacío, tamaño y content-type image/*; default 10 MiB. No expone validar(MultipartFile).
MinioService.guardar(MultipartFile,String) devuelve object key. construirUrlPublica(String) devuelve URL. El maestro habla de URLs y de validar prefijo como si fueran claves: cerrar representación de entrada/persistencia/salida antes de F6. El objetivo sigue siendo JPEG/PNG/WebP, 5 MB y máximo cuatro adjuntos; no reducir el límite global de otros módulos para lograrlo.
