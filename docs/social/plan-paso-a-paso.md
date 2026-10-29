# Plan paso a paso del módulo Social

Fecha: 23/10/2026. Estado: planificación de ejecución; los casos descritos no se han ejecutado.
Fuente normativa: [Documento Maestro v1.0](fuentes/documento-maestro-v1.0.md).
Complementos: [enmiendas](enmiendas.md), [contratos verificados](contratos-verificados.md)
y [estado real](estado.md). Este plan organiza el trabajo; no modifica el maestro.

## Cómo ejecutar el plan

1. Ejecutar primero los alcances backend B01–B11, en el orden indicado; reservar B12 para el cierre conjunto.
2. Cerrar los bloqueos del alcance antes de implementar la fase afectada.
3. Para cada fase: implementar, verificar, registrar evidencia y revisar antes de avanzar.
4. Después de aceptar el backend, ejecutar los alcances frontend U01–U09.
5. Terminar con la aceptación conjunta backend/frontend de F18.

Se conservan F0–F18 del maestro. Bxx.y y Uxx.y son subdivisiones de ejecución,
no reemplazos de esas fases. F4 se adelanta cuando F3 necesita sus contratos.
F18 tiene una revisión backend y un cierre final tras frontend.

Patrón solicitado: API pública → implementación → servicios internos, DTOs y
clientes adaptadores, como Alertas Clínicas con Nutricional/Aptitud Física.
Mantener llamadas directas de sincronización; su política transaccional debe acordarse.
No copiar automáticamente REQUIRES_NEW ni try/catch: probar también rollback del productor
y recuperación de una sincronización fallida para evitar publicaciones inconsistentes.

### Punto de partida

- F1 tiene preparados SocialApiService y sus dos DTOs; la compilación principal pasó.
- No hay funcionalidad social ni HU/RN acreditadas. F0 sigue abierto; F2–F18 pendientes.
- El test normal no compila por pruebas existentes incompatibles. El diagnóstico modular
  acotado detectó ciclos existentes; no equivale a una suite recompilada en verde.
- No iniciar dev con ddl-auto:create. La provisión y las pruebas Oracle requieren
  entorno aislado previamente acordado. H2 no acredita constraints de Oracle.
- El maestro propone REST/DTOs; la implementación actual todavía no los ofrece.

### Datos y JSON de prueba

El archivo [datos-prueba.json](datos-prueba.json) contiene cuerpos reutilizables J01–J19,
las identidades de prueba y las correspondencias con los casos de este plan.
Es un catálogo de ejemplos, no una colección ejecutable ni un seeder.

- IDs ficticios: preparar usuarios/equipos por mecanismos existentes y sustituirlos.
  Obtener IDs de posts/comentarios desde respuestas reales; no presuponer secuencias.
- Usar sesiones separadas: A autor activo; B miembro activo; C no miembro activo;
  D administrador global; E usuario inactivo; T administrador del equipo.
- idUsuario e idPersona son distintos. Resolver su relación por contrato, nunca por igualdad.
- Para REST: base propuesta `/api/v1/social`, JWT de prueba en Authorization y JSON en
  Content-Type. Nunca guardar tokens reales en este catálogo.
- GET y DELETE sin cuerpo se prueban con URL, identidad y estado previo; no requieren JSON.
- Sincronización por API Java: J01/J02 representan DTOs para fixtures del test;
  no son endpoints HTTP ni contratos de creación de actividades/noticias.
- Las URLs de ejemplo usan example.invalid. Reemplazar por referencias válidas del
  entorno; no descargar ni asumir objetos MinIO existentes.
- Éxitos: acordar códigos HTTP y envoltorio de respuesta al cerrar controllers/OpenAPI.
  No se inventan respuestas ni se presupone la serialización de Page.
- Errores especificados: 409 donde el maestro exige BusinessRuleException; 404 para
  ResourceNotFoundException. Otros rechazos requieren fijar el código en el contrato.

## Parte I — Alcances del backend

### B01 — Preparación, contratos y límites

**Fases:** F0, revisión de F1 y prerrequisitos de F4. **HU/RN:** transversal.

| Subfase | Trabajo y entrega |
|---|---|
| B01.1 | Cerrar C01/C02: conservar stack real, decidir entorno aislado, provisión/migración, permisos y recuperación. Preparar artefactos revisables; ejecutar solo sobre el entorno autorizado. |
| B01.2 | Cerrar C08/C09: acordar grafo con llamadas directas, nombres de interfaces y dependencias. Separar problemas globales existentes de los introducidos por Social, sin silenciar el test. |
| B01.3 | Revisar F1 y cerrar C03/C04/C07: dueño de equipos, membresías, permisos, relación usuario/persona, extensiones de actividades/noticias y manejo local de inactivos. Registrar contratos y enmiendas; luego crear adaptadores F4. |
| B01.4 | Crear estructura/configuración F0 y verificar límites, compilación y contratos. Definir cómo se repararán las pruebas globales con sus dueños; registrar bloqueo si siguen fallando. |

**Casos:** B01-T01 verificar que todas las futuras entidades usarán SLB_SOCIAL;
B01-T02 batch de 101 autores se divide en 100+1, sin pérdida ni asociación por posición;
B01-T03 autor inexistente/inactivo se representa con fallback solo al leer;
B01-T04 E no puede crear/editar/comentar/reaccionar/reportar y recibe 409 conforme a RN34;
B01-T05 repetir control con usuario activo válido; B01-T06 ejecutar análisis modular y
revisar imports API-only, entidades externas y ciclos; B01-T07 comprobar que un no miembro
no obtiene permiso por el solo hecho de existir el equipo; B01-T08 identidad del request
se obtiene de seguridad, nunca de autor enviado por cliente.

**Salida:** contratos reales documentados, F0 cerrado y estrategia verificable para
Modulith. La preferencia por un patrón existente no cierra por sí misma un ciclo.
**JSON:** identidades y J03 para acciones; el JWT no forma parte del cuerpo.

### B02 — Persistencia y modelo de 13 tablas

**Fase:** F2 y preparación de entidades auxiliares antes de sus servicios.
**Bloqueos:** C10/C15; decisiones de constraints, índices y nulabilidad.

| Subfase | Trabajo y entrega |
|---|---|
| B02.1 | Resolver título obligatorio frente a post solo imagen y política de share único tras borrado. Acordar persistencia de restricciones del maestro, no solo validaciones Java. |
| B02.2 | Implementar Post, PostImagen, Comentario, reacciones, enums y PK compuestas; repositorios base. |
| B02.3 | Incorporar las otras tablas antes de su consumidor: PostFijado B05; adjunto B06; hashtags/mención B08; vistas/guardados B07; reporte B09. Mantener total final de 13, sin tabla SHARE. |
| B02.4 | Probar repositorios/constraints en Oracle aislado: claves, relaciones internas, null, duplicados y concurrencia. |

**Casos:** B02-T01 duplicar tipo/ref de actividad o noticia no crea segunda fila;
B02-T02 repetir reacción usuario/target respeta PK; B02-T03 validar enum inválido;
B02-T04 reporte con ambos targets o ninguno se rechaza; B02-T05 validar scope GLOBAL
con scopeId null y scopes EQUIPO/ACTIVIDAD con ID; B02-T06 post manual solo imagen se
persiste según C10; B02-T07 constraints descritas en el modelo existen realmente en Oracle;
B02-T08 share duplicado se impide por UK_POST_AUTOR_ORIGEN.

**Salida:** persistencia base verificada y plan explícito para auxiliares.
**JSON:** J03/J05/J09/J14; para restricciones usar también fixtures de repositorio,
pues los requests no permiten construir todos los estados inválidos de BD.

### B03 — Sincronización y conexión con Actividades/Noticias

**Fases:** F3 → F15 → F16. **HU:** 01–03, 13, 36.
**RN:** 01–03, 07, 14, 33, 39. **Depende de:** B01/B02.

| Subfase | Trabajo y entrega |
|---|---|
| B03.1 | Implementar API de sincronización idempotente y ocultamiento; payloads completos, imágenes referenciadas, fechas y política transaccional explícita. |
| B03.2 | F15: conectar creación, iniciar, finalizar, cancelar, reprogramar, transferir coordinador, subir/eliminar logo y subir/eliminar imagen. Son diez puntos independientes a verificar. |
| B03.3 | F16: conectar publicación oficial y acordar las demás actualizaciones/ocultamientos con Noticias. No deducir oficialidad desde una categoría ni resumen desde subtítulo. |
| B03.4 | Probar contrato, transacciones, concurrencia y recuperación. Auto-pin se acepta definitivamente con B05. |

**Casos:** B03-T01 enviar J01 dos veces conserva ID y una fila; B03-T02 cambiar estado,
fecha, coordinador, logo y galería actualiza el mismo post; B03-T03 CANCELADA sigue visible;
B03-T04 J02 oficial produce post oficial; B03-T05 carreras de dos sincronizaciones no duplican;
B03-T06 fallo social y rollback del productor respetan la política acordada;
B03-T07 ocultar por ID externo afecta solo al tipo correcto; B03-T08 cambiar datos en cada
uno de los diez puntos reales se refleja en el muro; B03-T09 una sincronización posterior
a moderación no restaura contenido sin la política explícita acordada.

**Salida:** actividades/noticias integradas con evidencia de los diez puntos y sin
re-subir imágenes. J01/J02 son fixtures internos, no POST REST.

### B04 — Publicaciones manuales, permisos y CRUD

**Fase:** parte de F5. **HU:** 26, 34–36. **RN:** 08, 14–16, 34, 39–40.
**Bloqueos:** C10/C21 y contrato real de equipos.

| Subfase | Trabajo y entrega |
|---|---|
| B04.1 | Crear post personal y como equipo; autor autenticado; validar membresía, texto/imágenes y límites de campos. |
| B04.2 | Editar y eliminar contenido permitido. Edición por autor/admin de equipo/admin global según RN16; acordar permisos de eliminación por equipo antes de aplicarlos. |
| B04.3 | Probar autorización, visibilidad, solo imagen y protección de campos oficiales/autoría. |

**Casos:** B04-T01 A crea J03; B04-T02 B miembro crea J04; B04-T03 C no miembro no puede
crear como equipo; B04-T04 A edita J06 y otro usuario no autorizado no puede;
B04-T05 T puede editar según RN16; B04-T06 DELETE deja activo=false y no aparece en muro;
B04-T07 actividad/noticia no es editable/eliminable por CRUD del usuario;
B04-T08 J05 solo imagen se acepta conforme C10 y J07 vacío se rechaza;
B04-T09 intentar imponer autorId/esOficial no altera identidad/oficialidad;
B04-T10 probar límites título 150/151 y resumen 500/501.

**Salida:** CRUD y matriz de permisos verificados. **JSON:** J03–J07.

### B05 — Muro, filtros y fijados

**Fase:** resto de F5. **HU:** 04, 13, 15, 22–25.
**RN:** 08, 20, 33, 35–36. **Bloqueo:** C16.

| Subfase | Trabajo y entrega |
|---|---|
| B05.1 | Implementar MuroService, controllers, paginación, contadores agregados y enriquecimiento batch de autores. Definir desempate de fechas y respuesta Page. |
| B05.2 | Implementar tabs y scopes: equipo, actividad, hashtag; el filtro hashtag se completa con B08. Obtener membresías por API, sin MiembroEquipoView inventada. |
| B05.3 | Implementar FijadoService, máximo tres por scope, expiración, permisos y auto-pin oficial; acordar cupo compartido y restauración. |
| B05.4 | Probar orden, privacidad, límites de página, concurrencia y scopes independientes. |

Actualización E04: enriquecer equipos usando SocialEquipoApi.obtenerPerfilesPorIds;
mostrar nombre/imagen en autoría de equipo y reservar el perfil visual completo y
líder para cabecera/detalle del equipo. Datos en vivo, sin snapshots persistidos ni
re-subida de imágenes. Caso adicional B05-T11: perfil coincide con perfil-equipo-social.json,
cambios de imagen/líder se reflejan en la siguiente lectura y líderes ausentes tienen fallback.

**Casos:** B05-T01 PARA_TI y RECIENTES ordenan igual; B05-T02 páginas consecutivas y
empate de fecha respetan el criterio acordado; B05-T03 contenido inactivo se excluye;
B05-T04 MIS_EQUIPOS solo usa equipos del usuario; B05-T05 equipo/actividad muestra únicamente
sus pins; B05-T06 cuarto pin del mismo scope devuelve 409; B05-T07 pin expirado deja de
destacar según política; B05-T08 auto-pin oficial respeta cupo y no duplica al resincronizar;
B05-T09 dos fijados concurrentes no superan el cupo; B05-T10 detalle de autores ausentes
mantiene fallback sin persistir snapshots.

**Requests:** GET `/muro?tab=RECIENTES&page=0&size=20`, `/muro/equipos/{idEquipo}`,
`/muro/actividades/{idActividad}`; POST `/posts/{idPost}/fijar` con J14/J15;
DELETE `/posts/{idPost}/fijar?scopeTipo=GLOBAL` sin cuerpo.
**Salida:** feed y pins aceptados; no declarar cerrado feed hashtag antes de B08.

### B06 — Comentarios y adjuntos

**Fase:** F6. **HU:** 05–08, 14. **RN:** 04, 06, 12–13, 34.
**Bloqueos:** C05/C06/C11; acordar clave/URL y tombstone.

| Subfase | Trabajo y entrega |
|---|---|
| B06.1 | Crear/listar raíces y respuestas; validar que padre pertenece al post y no es respuesta. Definir paginación de raíces/respuestas. |
| B06.2 | Editar y eliminar con permisos, editado/fechaEdicion y cascada lógica. Implementar lectura del padre eliminado conforme a C11. |
| B06.3 | Subida multipart y referencias de adjuntos; JPEG/PNG/WebP, máximo cuatro y 5 MB cada uno. Acordar bytes exactos del límite y representación; aplicar localmente. |
| B06.4 | Probar contenido, autorización, hilos, objetos ajenos y fallos de almacenamiento; no borrar automáticamente adjuntos MinIO al soft-delete. |

**Casos:** B06-T01 J08 raíz y J09 respuesta; B06-T02 rechazar respuesta a respuesta;
B06-T03 rechazar padre de otro post; B06-T04 autor edita J10 y cambia marca/fecha;
B06-T05 autor/admin elimina, tercero no; B06-T06 raíz/respuestas quedan inactivas y se
renderiza tombstone según acuerdo; B06-T07 aceptar JPEG/PNG/WebP y rechazar otros MIME;
B06-T08 probar tamaño límite y límite+1 byte; B06-T09 rechazar quinto adjunto;
B06-T10 rechazar referencia de otro post o prefijo; B06-T11 fallo de subida no crea
comentario con referencia inexistente; B06-T12 texto 1000/1001 y contenido vacío según validación acordada.

**JSON:** J08–J11; J11 es plantilla pendiente de C05. El archivo binario se envía multipart,
no como base64 ni como JSON. **Salida:** hilo completo y adjuntos aislados por post.

### B07 — Reacciones, shares, guardados y alcance

**Fases:** F7 y F9. **HU:** 09–12, 15–16, 19, 32.
**RN:** 05, 09–11, 17–19, 27–32, 34, 37–38.
**Bloqueos:** C12/C13/C15; matriz de visibilidad del original compartido.

| Subfase | Trabajo y entrega |
|---|---|
| B07.1 | F7: reaccionar/reemplazar/quitar y listar usuarios paginados por tipo en posts y comentarios. |
| B07.2 | F9: crear SHARE al original, autor del share, contador consistente, edición/borrado y política de duplicados/restauración. |
| B07.3 | F9: guardar/quitar privados; registrar primera vista atómica solo en detalle; purga mensual 180 días sin recalcular total. |
| B07.4 | Probar carreras, identidad, original oculto, privacidad y comportamiento tras purga. |

**Casos:** B07-T01 J12 LIKE y luego J13 LOVE deja una reacción actualizada;
B07-T02 DELETE borra reacción físicamente; B07-T03 repetir para comentario y filtrar
lista de usuarios por tipo; B07-T04 rechazar tipo inválido y probar siete tipos válidos;
B07-T05 J16 sobre un share resuelve original, aumenta una sola vez y conserva autor que
comparte; B07-T06 original inactivo no se puede compartir; B07-T07 duplicado/borrado/
volver a compartir siguen C15; B07-T08 guardados de A no son visibles a B ni hay contador
público; B07-T09 GET detalle de C incrementa una vez, repetir no; B07-T10 autor/admin
no incrementan y GET muro tampoco; B07-T11 alcance visible solo autor/admin;
B07-T12 purgar historial no cambia total y nueva visita sigue C12;
B07-T13 solicitudes concurrentes del mismo usuario no duplican reacción, share o vista;
B07-T14 shares cuentan como publicaciones y renderizan el original según visibilidad acordada.

**JSON:** J12/J13/J16. Guardar/quitar y consultar detalle se hacen sin body.
**Salida:** contadores coherentes y privacidad comprobada, incluida concurrencia Oracle.

### B08 — Hashtags, menciones, búsqueda y perfiles

**Fases:** F8 → F10 → F11. **HU:** 17–18, 20–21, 27.
**RN:** 21–23, 31–32. **Bloqueos:** C14/C17 y contratos externos de búsqueda.

| Subfase | Trabajo y entrega |
|---|---|
| B08.1 | F8: persistir/normalizar hashtags y reparsear al editar. Implementar feed por tag y tendencia con periodo real. |
| B08.2 | F8: acordar tokens de USUARIO/ACTIVIDAD/EQUIPO, resolver IDs y autocomplete con APIs existentes/acordadas. No agregar @noticia. |
| B08.3 | F10: cerrar categoría/respuesta de comentarios antes de búsqueda unificada. Implementar filtros y paginación sin inventar un enum REST. |
| B08.4 | F11: perfil público con posts y comentarios visibles; mi contenido con publicaciones/comentarios/reacciones/guardados privados. Completar contrato si el DTO actual no cubre HU17. |
| B08.5 | Probar límites de texto, edición, categorías, privacidad y resultados vacíos. |

**Casos:** B08-T01 #Salud/#salud producen tag normalizado salud; B08-T02 longitudes
1/2/50/51 y caracteres permitidos; B08-T03 edición elimina asociaciones viejas y crea nuevas;
B08-T04 mención válida se resuelve y usuario inexistente/inactivo se ignora;
B08-T05 tokens de actividad/equipo se prueban solo con formato acordado;
B08-T06 búsqueda encuentra posts, comentarios, autores y hashtags;
B08-T07 perfil de A muestra posts y comentarios visibles, no guardados;
B08-T08 mi contenido separa cuatro tabs y nunca expone datos privados de otro;
B08-T09 trending respeta ventana temporal e invisibilidad conforme al contrato;
B08-T10 texto que contenga HTML/script no genera contenido ejecutable en el consumidor.

**Requests:** GET `/hashtags/salud/muro`, `/hashtags/tendencia?dias=7&limite=10`,
`/menciones/buscar?tipo=USUARIO&q=ana&limite=5`, `/buscar?q=salud&tipo=POST&page=0&size=20`,
`/usuarios/{idUsuario}/actividad`, `/mi-contenido?tab=GUARDADOS`.
**JSON:** J17 para usuario/hashtag; formatos de actividad/equipo pendientes.
**Salida:** HU20 y HU17 completas; no basta la categoría POST ni un perfil solo de posts.

### B09 — Reportes y moderación

**Fase:** F12. **HU:** 28–30. **RN:** 24–26, 34.

| Subfase | Trabajo y entrega |
|---|---|
| B09.1 | Crear reporte de post/comentario con target exclusivo y unicidad por usuario/target. |
| B09.2 | Cola admin y resolución aceptar/rechazar; registrar revisor, fecha y acción. Acordar cómo se conserva comentario de resolución del request. |
| B09.3 | Ocultar/restaurar posts/comentarios; acordar interacción con cascada, contenido auto-generado y sincronización posterior. |
| B09.4 | Probar permisos por rol, duplicados concurrentes y restauración sin efectos colaterales. |

**Casos:** B09-T01 J18 crea reporte; B09-T02 repetir devuelve 409;
B09-T03 mismo caso para comentario; B09-T04 D acepta J19 y se registra OCULTADO, contenido
invisible; B09-T05 rechazo deja contenido según estado previo, sin ocultarlo por el rechazo;
B09-T06 usuario común no accede a cola/resolución/ocultar/restaurar;
B09-T07 restauración no revive respuestas u originales ajenos a la operación;
B09-T08 repetir resolución sigue política acordada; B09-T09 target XOR se verifica
en servicio y persistencia, no mediante campos inventados en ReportarRequest.

**Salida:** moderación auditable. **JSON:** J18/J19 y variante estado RECHAZADO.

### B10 — Métricas y notificaciones

**Fases:** F13 y F14. **HU:** 31, 33.
**Bloqueos:** C18/C19, P01/P03/P05; definiciones exactas de métricas.

| Subfase | Trabajo y entrega |
|---|---|
| B10.1 | F13: definir usuarios activos, engagement, tendencias, periodo, zona horaria y contenido excluido; completar DTOs agregados antes de implementarlos. |
| B10.2 | F13: implementar agregados y acceso admin. Comparar contra un conjunto de datos con resultado calculado previamente. |
| B10.3 | F14: acordar transporte, códigos, plantillas, deep links, destinatarios y mapeo usuario/persona. Preparar cinco eventos del maestro. |
| B10.4 | F14: entrega AFTER_COMMIT, sin autoavisos ni duplicados por acción; definir reintentos/deduplicación y probar rollback/fallos. |

**Casos:** B10-T01 agregados coinciden con fixture y rango temporal;
B10-T02 solo admin accede a métricas; B10-T03 comentario raíz notifica autor del post;
B10-T04 respuesta notifica autor del padre; B10-T05 mención notifica al mencionado;
B10-T06 reacción de post notifica autor; B10-T07 publicación de equipo notifica miembros
excepto autor solo si existe contrato de destinatarios; B10-T08 no hay autoaviso;
B10-T09 rollback no entrega evento; B10-T10 reintento no duplica entrega conforme acuerdo;
B10-T11 idUsuario distinto de idPersona llega al destinatario correcto;
B10-T12 enlace abre el contenido correcto según permisos/estado.

**Requests/fixtures:** GET `/admin/metricas?desde=2026-10-01&hasta=2026-10-31`;
generar acciones con J08/J09/J12/J17/J04. Eventos son objetos internos, no POST REST.
**Salida:** HU33 completa o limitación explícita aceptada. Evento de equipo con lista vacía
no cuenta como notificación a miembros implementada.

### B11 — Datos demostrativos y aceptación backend

**Fases:** F17 y revisión backend de F18. **Depende de:** B01–B10.

| Subfase | Trabajo y entrega |
|---|---|
| B11.1 | Seeder opcional en entorno dev aislado y autorizado; idempotente, sin activación fuera de perfil. No usarlo para probar la BD existente. |
| B11.2 | Completar OpenAPI de todos los grupos, errores y paginación. Fijar códigos de éxito y serialización antes del frontend. |
| B11.3 | Compilar fuentes/pruebas desde cero y ejecutar pruebas pertinentes, integración Oracle y Modulith con resultado real. Resolver bloqueos globales con dueños, sin ocultarlos. |
| B11.4 | Recorrer los casos B01–B10 con identidades separadas; exportar evidencia y contratos aceptados para frontend. |

**Casos:** B11-T01 ejecutar seeder dos veces no duplica; B11-T02 otro perfil no lo activa;
B11-T03 OpenAPI coincide con firmas/respuestas reales; B11-T04 flujo crear actividad →
muro → detalle → comentario → respuesta → reacción → share → guardado → búsqueda →
reporte → moderación → actualización de actividad; B11-T05 reinicio del entorno de
prueba según estrategia acordada conserva o reconstruye únicamente lo esperado;
B11-T06 scripts de prueba no contienen secretos.

**Salida:** backend aceptado o bloqueos concretos declarados. No afirmar F18 global
terminada hasta completar frontend y B12.

### B12 — Cierre conjunto y regresión final

**Fase:** cierre de F18, después de U01–U09.

| Subfase | Trabajo y entrega |
|---|---|
| B12.1 | Ejecutar recorrido E2E desde Angular con backend real en entorno aislado. |
| B12.2 | Verificar regresión de login, layout, Alertas, Nutricional, Aptitud Física, actividades, noticias y notificaciones. |
| B12.3 | Registrar por cada HU/RN: casos ejecutados, resultado, evidencia y pendientes. Resolver bloqueos antes de declarar completo. |

**Casos:** B12-T01 completar flujos con cada identidad; B12-T02 navegar a URL directa con
y sin permisos; B12-T03 fallo de servicio muestra estado recuperable sin datos privados;
B12-T04 build backend/frontend y pruebas exigidas pasan; B12-T05 no existen contratos
ficticios productivos ni bypass de autorización/modularidad.
**Salida:** 36 HU y 40 RN verificadas, o desviaciones expresamente aceptadas y listadas.

## Parte II — Alcances del frontend

Estas fases Uxx son propuestas de ejecución Angular derivadas de las HU. El maestro
no fija rutas de pantallas ni diseño visual. Confirmar respuestas REST antes de tiparlas.
En todos los alcances: estados cargando/error/vacío, teclado, foco, diseño adaptable y
contenido de texto seguro. Los permisos del backend siguen siendo la autoridad.

### U01 — Estructura e integración Angular

**Depende de:** B11. **HU:** transversal. **Referencia backend:** F18.

| Subfase | Entrega |
|---|---|
| U01.1 | Revisar componentes/patrones de Alertas, Nutricional y Aptitud Física; definir estructura features/social y navegación coherente con layout existente. |
| U01.2 | Modelos TypeScript tomados del contrato real, servicios HttpClient y rutas lazy; reutilizar entorno, AuthGuard, RoleGuard e interceptores. |
| U01.3 | Pruebas de navegación, carga/error/vacío y contrato HTTP sin reimplementar Keycloak. |

**Casos:** U01-T01 sesión válida entra; U01-T02 sin sesión sigue login existente;
U01-T03 ruta admin rechaza rol común; U01-T04 cliente usa URL/headers/interceptores reales;
U01-T05 error 404/409 se presenta sin filtrar datos. **JSON:** reutilizar catálogo REST;
los mocks de prueba son solo de pruebas, nunca contratos productivos inventados.

### U02 — Muro, tarjetas, filtros y fijados

**Depende de:** U01, B03/B05/B08. **HU:** 01–04, 13, 15, 21–25.

| Subfase | Entrega |
|---|---|
| U02.1 | Tarjetas de actividad/noticia/manual/share, autor/fallback, oficialidad y contadores. |
| U02.2 | Tabs, paginación, equipo/actividad/hashtag, pins del scope y acción fijar con permisos. |
| U02.3 | Pruebas de orden, filtros, carga, error, vacío, responsive y teclado. |

**Casos:** U02-T01 PARA_TI/RECIENTES mantienen cronología; U02-T02 cambiar tab reinicia
página según contrato; U02-T03 pins de otro scope no aparecen; U02-T04 oficial destaca;
U02-T05 autor ausente muestra fallback; U02-T06 cuarto pin presenta 409 sin desincronizar
lista; U02-T07 no hay contador público de guardados/alcance. **Datos:** J01/J02/J03/J14/J15.

### U03 — Editor y permisos de publicaciones

**Depende de:** U01/U02, B04. **HU:** 26, 34–36.

| Subfase | Entrega |
|---|---|
| U03.1 | Crear personal/equipo con opciones de equipos reales del usuario. |
| U03.2 | Editar, confirmar eliminación y soportar solo imagen conforme C10. No añadir un upload de posts que el contrato no define. |
| U03.3 | Pruebas de formularios, límites, permisos y recuperación tras fallo del servidor. |

**Casos:** U03-T01 personal J03/equipo J04; U03-T02 vacío inválido y solo imagen válido;
U03-T03 no habilitar edición/borrado de actividad/noticia; U03-T04 tercero no puede
editar aunque fuerce request; U03-T05 error conserva borrador; U03-T06 doble clic no
duplica envío; U03-T07 se aplican máximos 150/500. **JSON:** J03–J07.

### U04 — Detalle, comentarios y adjuntos

**Depende de:** U02, B06. **HU:** 05–08, 14.

| Subfase | Entrega |
|---|---|
| U04.1 | Detalle y raíces/respuestas de un nivel con paginación acordada. |
| U04.2 | Crear/editar/eliminar con permisos, marca editado y tombstone definido; subida multipart y hasta cuatro adjuntos. |
| U04.3 | Pruebas de hilo, archivos, teclado, fallos parciales y referencia devuelta por upload. |

**Casos:** U04-T01 raíz/respuesta; U04-T02 no ofrecer nietos; U04-T03 tombstone no expone
texto eliminado; U04-T04 quinto archivo/MIME/tamaño se detectan y backend revalida;
U04-T05 fallo de un upload no envía referencias inválidas; U04-T06 cancelar/eliminar no
promete borrar MinIO; U04-T07 editar marca fecha correctamente. **JSON:** J08–J11.

### U05 — Reacciones, compartir, guardar y alcance

**Depende de:** U02/U04, B07. **HU:** 09–12, 15–16, 19, 32.

| Subfase | Entrega |
|---|---|
| U05.1 | Selector de siete reacciones, reemplazo/quitar y lista paginada por tipo en post/comentario. |
| U05.2 | Share con texto/equipo, render original, guardar/quitar y alcance restringido. |
| U05.3 | Pruebas de respuestas tardías, contadores, duplicados, rollback de UI y privacidad. |

**Casos:** U05-T01 LIKE→LOVE deja una selección; U05-T02 lista filtrada/paginada;
U05-T03 error revierte actualización optimista si se utiliza; U05-T04 original oculto
se trata según contrato; U05-T05 guardado solo en cuenta propia;
U05-T06 alcance no se muestra a terceros ni se registra desde tarjetas;
U05-T07 contador de share corresponde al original. **JSON:** J12/J13/J16.

### U06 — Menciones, hashtags y búsqueda

**Depende de:** U03/U04, B08. **HU:** 20–21, 27.

| Subfase | Entrega |
|---|---|
| U06.1 | Autocomplete de usuario/actividad/equipo con tokens acordados y navegación segura por teclado. |
| U06.2 | Hashtags navegables y búsqueda por las cuatro categorías aceptadas, incluidos comentarios. |
| U06.3 | Pruebas de texto seguro, consultas vacías, cambios rápidos y ausencia de resultados. |

**Casos:** U06-T01 selección por teclado conserva token/ID acordados;
U06-T02 respuesta de búsqueda antigua no reemplaza la nueva; U06-T03 #Salud navega a
salud; U06-T04 comentarios aparecen como resultados; U06-T05 menciones inexistentes no
rompen el editor; U06-T06 HTML/script se muestra de forma segura. **JSON:** J17;
no inventar tokens de actividad/equipo antes de C17.

### U07 — Perfil público y mi contenido

**Depende de:** U02/U04/U05, B08. **HU:** 17–19.

| Subfase | Entrega |
|---|---|
| U07.1 | Perfil público con identidad, publicaciones y comentarios visibles. |
| U07.2 | Cuatro tabs propias: publicaciones, comentarios, reacciones y guardados. |
| U07.3 | Pruebas de navegación, paginación, cuenta distinta y contenido oculto/eliminado. |

**Casos:** U07-T01 perfil público no revela guardados; U07-T02 cada tab consume su filtro
real; U07-T03 eliminar contenido actualiza listado; U07-T04 cambiar cuenta no conserva
datos privados de la anterior; U07-T05 autor inactivo mantiene fallback.
**Datos:** contenido creado con J03/J08/J12 y guardados mediante requests sin body.

### U08 — Reportes, moderación y métricas

**Depende de:** U02/U04, B09/B10. **HU:** 28–31.

| Subfase | Entrega |
|---|---|
| U08.1 | Reporte con motivo/detalle y tratamiento de duplicado 409. |
| U08.2 | Cola/resolución/ocultar/restaurar para admin con confirmaciones y estados auditables. |
| U08.3 | Panel de métricas por fechas reutilizando ECharts cuando corresponda; probar roles y agregados reales. |

**Casos:** U08-T01 reportar post/comentario; U08-T02 duplicado no crea otra fila;
U08-T03 aceptar/rechazar refleja estado; U08-T04 usuario común no entra por URL directa;
U08-T05 gráfico coincide con valores recibidos, incluida colección vacía;
U08-T06 rango inválido se trata según contrato. **JSON:** J18/J19 y estado RECHAZADO.

### U09 — Notificaciones y aceptación frontend

**Depende de:** U01–U08, B10/B11. **HU:** 33 y regresión de todas.

| Subfase | Entrega |
|---|---|
| U09.1 | Integrar deep links acordados con el centro de notificaciones existente; no crear un segundo sistema. |
| U09.2 | Build, pruebas de componentes/servicios y recorrido integrado con backend real. |
| U09.3 | Accesibilidad por teclado/foco, adaptación móvil/escritorio y regresión de navegación existente; entregar evidencia a B12. |

**Casos:** U09-T01 aviso abre post/comentario correcto; U09-T02 contenido oculto o sin
permiso muestra respuesta adecuada; U09-T03 recarga de URL conserva navegación;
U09-T04 flujo completo con autor/miembro/no miembro/admin/inactivo;
U09-T05 controles interactivos funcionan por teclado; U09-T06 build y pruebas pasan.
**Datos:** acciones del catálogo producen avisos reales solo después de F14.

## Trazabilidad completa

| HU | Alcance responsable de aceptación |
|---|---|
| 01–03 | B03, U02 |
| 04 | B05, U02 |
| 05–08 | B06, U04 |
| 09–11 | B07, U05 |
| 12 | B07, U05 |
| 13 | B03/B05, U02 |
| 14 | B06, U04 |
| 15–16 | B07/B05, U02/U05 |
| 17–18 | B08, U07 |
| 19 | B07, U05/U07 |
| 20–21 | B08, U06 |
| 22–25 | B05, U02 |
| 26 | B04, U03 |
| 27 | B08, U06 |
| 28–30 | B09, U08 |
| 31 | B10, U08 |
| 32 | B07, U05 |
| 33 | B10, U09 |
| 34–36 | B04/B03, U03 |

| RN | Casos principales |
|---|---|
| 01–03 | B02-T01, B03-T01/T02/T05 |
| 04 | B06-T05/T06 |
| 05 | B07-T02 |
| 06 | B06-T05 |
| 07 | B03-T03 |
| 08 | B04-T06, B05-T03 |
| 09–11 | B07-T01/T03/T13 |
| 12 | B06-T01/T02/T03 |
| 13 | B06-T06 |
| 14 | B04-T07 |
| 15–16 | B04-T02/T03/T04/T05 |
| 17–19 | B07-T05/T06/T07/T13 |
| 20 | B05-T06/T09 |
| 21 | B08-T01/T02 |
| 22–23 | B08-T03/T04/T05 |
| 24–26 | B02-T04, B09-T01/T02/T03/T04/T09 |
| 27–30 | B07-T09/T10/T12/T13 |
| 31–32 | B07-T08/T11, B08-T07/T08 |
| 33 | B03-T04, B05-T08 |
| 34 | B01-T04 |
| 35–36 | B05-T01/T05 |
| 37–38 | B07-T05/T14 |
| 39 | B03-T04, B04-T09 |
| 40 | B02-T06, B04-T08 |

## Lista de decisiones por momento de ejecución

| Momento | Decisiones pendientes |
|---|---|
| Antes de B01/F0 | C02 Oracle aislado; C08 grafo con llamadas directas; C09 declaración de dependencias. C01 conserva stack real. |
| Antes de B02/F2 | C10 solo imagen/título; C15 restricciones y ciclo de vida del share. |
| Antes de B03/B04 | C03/C04 contratos reales y dueño de equipos; C07 rechazo de inactivos; C21 permisos de borrado; transacciones y ocultamiento frente a resincronización. |
| Antes de B05 | C16 cupos, auto-pin, expiración/concurrencia; C20 prerrequisitos de entidades/servicios adelantados. |
| Antes de B06 | C05/C06 claves/URLs, MIME/tamaño local; C11 eliminación/render de respuestas. |
| Antes de B07 | C12 vistas tras purga; C13 ratificar alcance autor/admin; C15 duplicado/restore/contadores. |
| Antes de B08 | C14 comentarios en búsqueda; C17 tokens; respuestas de perfil público y métricas de tendencia. |
| Antes de B09/B10 | Restauración/auditoría; C18/C19 y P01/P03/P05 de notificaciones; definición de métricas. |
| Antes de U01 | OpenAPI real aceptada: códigos, errores, Page, fechas, null, permisos y referencias de archivos; rutas UI y deep links acordados. |

## Evidencia obligatoria al terminar cada fase

Registrar en estado.md: alcance/subfase, F original, archivos, decisiones, HU/RN,
casos ejecutados con identidad y datos utilizados, comando o pasos, resultado,
evidencia y pendientes. Un caso bloqueado no cuenta como aprobado.

Comandos de referencia para ejecución futura, no ejecutados por este plan:

```powershell
# Backend: compilar sin iniciar aplicación.
.\mvnw.cmd -o -DskipTests compile
# Tras reparar incompatibilidades de test: verificación modular recompilada.
.\mvnw.cmd -o '-Dtest=ModularityTests#verifiesModularStructure' test
# Frontend, desde su carpeta y con dependencias existentes.
npm run build
npm test -- --watch=false
```

Las pruebas de integración Oracle necesitan su configuración aislada acordada;
no se sustituye esa preparación con un spring-boot:run sobre dev.

Primer encargo sugerido: ejecutar B01.1–B01.4, presentar contratos y bloqueos resueltos,
y después continuar B02. Este documento no ejecuta fases ni SQL.
