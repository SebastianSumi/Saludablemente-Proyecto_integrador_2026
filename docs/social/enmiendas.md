# Enmiendas y acuerdos de implementación

## E07 - Limpieza de servicios externos (29/10/2026)

El usuario ordenó retirar Keycloak, MinIO, RabbitMQ y Ollama; Oracle se conserva
temporalmente hasta una migración posterior a PostgreSQL nativo. Se implementó
autenticación propia con JWT HS256, se dejó el almacenamiento de imágenes como
stub sin proveedor, se pasó el despacho de notificaciones a síncrono in-process
con DLQ en tabla y se eliminó Ollama dejando Gemini por HTTP.

No se ejecutaron compilaciones, pruebas, `npm install`, backend ni DDL. Quedan
pendientes el proveedor de almacenamiento (bloquea adjuntos de comentarios y
subidas de imágenes), el endurecimiento de sesión y el cambio de contraseña
desde perfil. Detalle en `../limpieza-servicios-externos.md`.

## E04 — Ampliación independiente de Teams (23/10/2026)

El usuario solicitó implementar campos de identidad visual, imagen MinIO y liderazgo,
y corrigió expresamente el esquema a `SBL_TEAM` (no `SLB_TEAM`). Se cambia únicamente
el mapeo de Team; se prepara una migración manual que conserva IDs y contempla las FK
heredadas. No se ejecuta DDL ni se provisiona una BD existente con el perfil dev.

Teams sigue siendo dueño del equipo; Personal mantiene su membresía actual.
La asignación del líder usa idUsuario externo Long, sin relación JPA a Seguridad.
Solo administrador asigna/retira líder; administrador o líder activo y miembro vigente
edita el equipo activo. El puerto TeamAccesoApi se declara en Teams y se implementa
en Seguridad componiendo APIs públicas existentes, sin añadir Teams→Seguridad/Personal.
No se cambia el contrato SocialEquipoApi ni su regla histórica de Coordinador.

Las imágenes pertenecen a Teams, bajo `teams/{id}/logo/`, reutilizando almacenamiento
MinIO existente. No se aplica la restricción de prefijos de adjuntos de Social al logo
de otro módulo. Oracle persiste clave; respuestas exponen URL. Límite local 5 MiB,
JPEG/PNG/WebP. No cambia configuración global, multipart o autenticación.

Esta enmienda documenta la autorización de este encargo; no cierra F0, C08 ni ninguna
HU/RN de Social. Detalle y límites en `../teams/README.md`.

## E01 — Autorización y patrón de integración (23/10/2026)

El usuario solicita implementar el alcance completo del maestro y seguir los patrones
de Alertas Clínicas con Nutricional y Aptitud Física. El adjunto recibido tiene el
mismo SHA-256 que fuentes/documento-maestro-v1.0.md:
`63E2488FD3465AF45711E6B6697616B77D139ED9ADFE9F9AF40F13DB0B99B6A6`.

Se mantienen las llamadas directas a SocialApiService del maestro. No se considera
autorizada la alternativa de sustituirlas por eventos de sincronización.
Los eventos sociales para notificaciones mantienen su finalidad independiente.

Patrones observados: API pública, DTOs, implementación delegada a servicios internos,
inyección por constructor y clientes de integración que adaptan APIs públicas.
MotorAlertasApiServiceImpl utiliza REQUIRES_NEW; los productores capturan errores
localmente. Esto no se adopta automáticamente para Social: F3 debe precisar consistencia,
commit/rollback y recuperación antes de integrar productores en F15/F16.

Se prepara F1 de forma independiente: únicamente interfaz y records exactos de §10,
sin implementación vacía ni consumidores conectados. No acredita HU01–03 ni cierra F0.
No se inventan contratos de equipos, actividades, noticias o notificaciones.

## E02 — Correcciones documentales comprobadas (23/10/2026)

- C01: conservar Java 21, Boot 4.1.1 y Modulith 2.1.1 del proyecto real.
- C15: §6.2.1 sí declara UK_POST_AUTOR_ORIGEN (AUTOR_ID, ORIGEN_POST_ID).
  Quedan pendientes borrado, restauración, volver a compartir y contadores.
- El ejemplo MiembroEquipoView de §8 no prueba una vista existente. Las membresías
  deben obtenerse por API pública, respetando los límites del módulo.
- El AGENTS.md raíz existe actualmente; la observación histórica del inventario
  sobre su ausencia no representa el estado actual.

## Decisiones pendientes que mantienen F0 abierto

- C02: entorno Oracle aislado y provisión/migración; no arrancar dev con create,
  no ejecutar SQL ni modificar la configuración existente sin cerrar esa modalidad.
- C08: comprobar el grafo completo. Alertas también importa DTOs de los módulos
  que lo invocan; copiar ese patrón no demuestra ausencia de ciclos Modulith.
- C09: usar como referencia la notación modulo::interfaz observada en Asistencias;
  comprobar nombres reales y exposición antes de declarar las dependencias de Social.

Esta enmienda no declara resueltos C02/C08/C09 ni da por aprobadas firmas externas.

## Evidencia de verificación del patrón

La compilación principal pasa. La ejecución normal del test modular queda detenida
por errores de compilación de pruebas existentes ajenas a Social. La invocación
directa a surefire del método verifiesModularStructure (clase de test previamente
compilada, fuentes principales recién compiladas) sí ejecuta el análisis y falla
con Violations: entre otros, detecta alertaclinica ↔ aptitudfisica y
actividades ↔ asistencias. Por tanto, el patrón indicado es real, pero el estado
global de Modulith no está verde. No se silencian esas verificaciones ni se
refactorizan módulos ajenos como parte de la preparación de F1.

## E03 — Contratos implementables y reglas autorizadas (23/10/2026)

El usuario autoriza cerrar primero los contratos y confirma expresamente:
Noticias tendrá oficialidad explícita (default false); el administrador de equipo
es el usuario activo con rol Coordinador cuya persona activa pertenece al equipo activo.
El administrador global no se convierte implícitamente en miembro/admin de un equipo.

Se conservan los seis métodos de SocialUsuarioApi v1.2. Se añade una API de identidad
complementaria para resolver usuario/persona y destinatarios, sin equiparar IDs.
Personal expone la membresía por idPersona; Seguridad implementa la composición
SocialEquipoApi usando su propio repositorio y APIs de Personal/Teams. Esto evita
añadir una dependencia Personal→Seguridad, ya que Seguridad ya depende de Personal.
Las membresías actuales son de un equipo por persona; no se inventa una tabla N:M.
El método adicional miembrosActivosDeEquipo devuelve IDs de cuentas activas vinculadas
a personas activas del equipo activo; totalMiembros cuenta personas activas.

Actividades y Noticias exponen APIs dedicadas en api/social sin reemplazar snapshots
existentes. Ausencia: Optional.empty; búsquedas vacías: lista vacía; límites 1–20.
Se reserva ID_EQUIPO_ACTIVIDAD nullable como atribución explícita de la actividad:
no se infiere del equipo del creador; filas históricas sin atribución devuelven null.
Su asignación desde el flujo dueño queda para F15, no se simula en los contratos.
Noticias expone resumen explícito opcional (no deriva subtítulo/contenido),
idEquipoAutor opcional distinto de los equipos destinatarios y urlCompleta opcional.
Estas extensiones requieren migración revisada antes de usar una BD existente;
no se ejecuta DDL ni se arranca dev por esta tarea.

El almacenamiento social retorna clave y URL por separado. Se persiste/transmite la
URL canónica del servidor en adjuntosUrls; se valida pertenencia exacta al prefijo,
rechazando traversal, otra autoridad, fragmentos, query y codificación ambigua.
La validación social es JPEG/PNG/WebP y 5 MiB (5*1024*1024 bytes), cuatro adjuntos;
no cambia límites globales ni las reglas de uploads existentes.

Notificaciones: Spring events en el proceso, listener AFTER_COMMIT en Social y
NotificacionApi como puente al RabbitMQ existente. Cinco códigos SOCIAL_* y variables
idPost/idComentario/idEquipo según evento; idPersona se resuelve por identidad.
Las rutas de pantalla se mantienen pendientes: se transmiten IDs para navegación,
no se inventan URLs Angular. Reintentos/deduplicación durable y provisión de plantillas
se verificarán al implementar F14; cerrar firmas no acredita entrega ni HU33.
Los eventos no se emiten desde los módulos dueños para sustituir llamadas directas.

Los enums referidos por los eventos (TipoReaccion y TipoContenidoMencion) se ubican
en social.api.dto, dentro de una interfaz nombrada pública, para no filtrar tipos
internos de las futuras entidades a los consumidores de los eventos.

Esta enmienda cierra definiciones de C03/C04/C05/C06/C07/C18/C19 para implementar
sus contratos, sin declarar completadas las fases funcionales, Oracle ni C08.

## E04 — Perfil visual y líder de equipo para Social (23/10/2026)

El usuario pide mostrar en Social imagen URL y líder, además de los datos actuales
de Teams. TeamApiService ya expone TeamPerfilDTO; se añade consulta batch de perfiles
sin cambiar TeamSnapshotDTO ni sus consumidores actuales.

SocialEquipoApi incorpora perfil completo individual y batch (máximo 100 IDs), con
imagenUrl, color, slogan, referenciaBiblica, textoVersiculo e idUsuarioLider.
El líder se enriquece en vivo mediante SocialUsuarioApi, en lote por IDs y sin exponer
email, roles, claves MinIO ni entidades. Cuenta ausente/inactiva: placeholder de líder;
equipo sin líder: lider=null. Los DTOs no persisten identidad ni imágenes.
Los campos logoUrl/logoEquipoUrl existentes pasan a contener la imagen URL de Teams
en snapshots, membresías y búsquedas; ya no se fuerzan a null.

La designación visual de líder no modifica la regla de permisos de E03: ser líder
no concede por sí solo administración; Coordinador activo y miembro sigue siendo
la condición autorizada. La visualización se completará en respuestas/pantallas Social.
Crear/editar Teams no genera posts automáticamente: Teams aporta identidad del equipo
y Social permitirá publicaciones en su nombre según membresía. Noticias todavía no
invoca sincronizarPostDeNoticia; esa conexión requiere F3/F16 y un receptor real,
sin introducir implementaciones vacías ni llamadas a un bean inexistente.

## E05 — Ejecución del backend completo (23/10/2026)

Autorización explícita del usuario para implementar todo el backend siguiendo
los patrones existentes. Decisiones de implementación para resolver ambigüedades:

- F0: esquema explícito SLB_SOCIAL; configuración y pruebas propias aisladas en H2
  modo Oracle, sin iniciar la aplicación ni ejecutar SQL en Oracle existente.
  La instalación Oracle real no se declara verificada por estas pruebas.
- C08/C09: llamadas directas mediante API como solicitó el usuario; dependencias
  module::namedInterface. No se desactiva modules.verify. El diseño bidireccional
  del maestro y los ciclos existentes se reportan como limitación arquitectónica,
  no como verificación verde ni como motivo para inventar un bus diferente.
- C10: TITULO admite null para cumplir RN40 (post de solo imagen). Con texto,
  se exigen título y resumen no blancos. No se inventa un título de reemplazo.
- C11: eliminación de raíz desactiva raíz y respuestas; al consultar se conserva
  un tombstone de la raíz si tenía respuestas, sin exponer textos/adjuntos eliminados.
- C12: la deduplicación de vistas dura mientras exista POST_VISTA; después de purga
  a 180 días una nueva visita cuenta de nuevo. El total histórico es acumulado,
  no una cardinalidad vitalicia de usuarios. Esta precisión modifica RN27 en ese borde.
- C13: alcance únicamente autor/admin, nunca público.
- C14: búsqueda añade tipo COMENTARIO; perfil público incluye comentarios paginados.
- C15: share activo duplicado produce 409. Compartir después de borrarlo restaura
  el mismo share y su ID; contador refleja shares activos. Original inactivo oculta
  sus shares en lecturas públicas, sin borrar físicamente esos registros.
- C16: cupo de tres pins activos/no expirados por scope, incluidos auto-pins. Auto-pin
  no desplaza pins existentes si el cupo está lleno. Operaciones de pin se serializan
  con bloqueo común de un post existente; expiración se aplica en lectura/cupo.
- C17: tokens @username, @actividad{ID} y @equipo{ID}; sin @noticia. Reparseo al editar.
- C21: borrar manual/share por autor o admin global; admin de equipo puede editar
  según RN16, no adquiere borrado ajeno. Moderación y resincronización preservan
  ocultamiento; solo restauración administrativa revive contenido moderado.
- La sincronización participa en la transacción del productor (REQUIRED), de modo
  que rollback del productor no deja un post huérfano. No se copia REQUIRES_NEW del
  motor de alertas: en Social ambas escrituras deben ser atómicas en la misma BD.
- F14: eventos después del commit y puente RabbitMQ existente. No se promete
  exactly-once ni reintentos durables de publicación sin infraestructura persistente;
  el fallo se registra y se reporta como límite explícito de entrega externa.

DTOs de respuesta permanecen separados de entidades; los datos de autor/equipo/líder
se enriquecen en vivo mediante las APIs ya implementadas. No se crean entidades
externas ni relaciones entre esquemas desde Social.

## E06 — Unicidad condicional comprobada en persistencia (23/10/2026)

Las pruebas H2 modo Oracle demostraron que las UK compuestas del maestro con
componentes NULL restringen indebidamente manuales/shares/reportes de otros targets.
POST conserva sus IDs lógicos, pero UK_POST_TIPO_REF usa SYNC_CLAVE (tipo:ref solo
en actividad/noticia) y UK_POST_AUTOR_ORIGEN usa SHARE_CLAVE (autor:origen solo en SHARE).
Las demás filas mantienen esas claves totalmente NULL. Las claves se calculan en
callbacks JPA, nunca desde requests. POST_REPORTE usa REPORTE_CLAVE usuario:P/C:target.
No se eliminan restricciones para hacer pasar pruebas ni se añade tabla SHARE.
La compatibilidad Oracle se debe certificar en el entorno real; no se infiere de H2.
Se añade OCULTO_MODERACION para distinguir ocultamiento administrativo de despublicación
del origen, y COMENTARIO_REVISION para conservar la resolución enviada por el admin.
