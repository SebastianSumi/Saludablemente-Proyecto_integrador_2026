# Estado al 29/10/2026

## Limpieza de servicios externos - 29/10/2026

- Decisión del usuario: eliminar Keycloak, MinIO, RabbitMQ y Ollama; conservar
  Oracle de forma temporal. Detalle en
  [limpieza-servicios-externos.md](../limpieza-servicios-externos.md).
- Backend: JWT propio HS256 (`JwtConfig`, `AuthService`, `AuthController`,
  `POST /api/v1/auth/login` y `/logout`); se retiraron KeycloakAdminConfig,
  KeycloakAdapter, MinioConfig, MinioService, RabbitConfig, NotificacionListener
  y NotificacionMensaje. `AlmacenamientoImagenService` quedó como stub y
  `NotificacionApiImpl` despacha in-app/email de forma síncrona con DLQ en tabla.
- Frontend: `AuthService` con HttpClient y `localStorage` (`slb.auth.session`),
  guard e interceptor propios, formulario de login con credenciales y limpieza
  de referencias a Keycloak/MinIO. El cambio de contraseña desde perfil queda
  pendiente y documentado.
- Configuración: `docker-compose.yml` solo contiene Oracle; se retiraron las
  dependencias Maven de Keycloak, MinIO, AMQP y google-genai. Secretos JWT y
  Gemini se leen de `JWT_SECRET` y `GEMINI_API_KEY`.
- Archivos principales: `seguridad/config/JwtConfig.java`,
  `seguridad/auth/AuthService.java`, `seguridad/auth/AuthController.java`,
  `shared/storage/AlmacenamientoImagenService.java`,
  `notificaciones/api/NotificacionApiImpl.java`, frontend
  `core/auth/auth-service.ts`, `core/auth/auth-guard.ts`,
  `core/interceptors/auth-token-interceptor.ts` y `core/login/login.ts`.
- Sin ejecutar compilación, pruebas, `npm install`, backend ni DDL. No se
  acreditan HU/RN de Social ni la integración Oracle.
- Pendiente autorizado para fase posterior: migrar Oracle a PostgreSQL nativo
  y evaluar un backend equivalente en Node.js.

## Corrección del formato del script — 23/10/2026

Por instrucción expresa del usuario, SCRIPTS/S00_01_preparar_backend.sql
reemplaza la variante dinámica anterior por el formato del adjunto: GRANT DBA
a SALUDABLEMENTE_APP primero; después 14 CREATE USER con sus permisos y cuota
en USERS. La cuenta de aplicación debe existir. Sin detección de tablespaces
ni manejo de usuarios existentes. No se ejecutó SQL. Esta corrección sustituye
las características de portabilidad/reejecución descritas en la entrada anterior.


## Aprovisionamiento portable de esquemas — 23/10/2026

- Solicitud posterior: adaptar adjunto de creación de usuarios para usar Hibernate
  ddl-auto:create y conceder DBA a SALUDABLEMENTE_APP antes de provisionar módulos.
- Archivo completo: `SCRIPTS/S00_01_preparar_backend.sql`; instrucciones
  en docs/teams/README.md. Crea cuenta de aplicación si falta y 14 esquemas, incluido
  SBL_TEAM exacto, recomendaciones IA y social; usa tablespaces predeterminados.
- Reejecución conserva claves/estado de cuentas existentes; claves iniciales nuevas
  usan el valor de desarrollo aportado expresamente por el usuario, no configuración.
- Verificación estática: todos los 13 esquemas de entidades actuales incluidos,
  SLB_SOCIAL adicional, DBA previo a módulos y ninguna sentencia CREATE TABLE.
- No se ejecutó Oracle, no hay clientes sqlplus/sqlcl disponibles en esta sesión.
  No se arrancó el backend ni se cambió YAML. No acredita pruebas de instalación
  Oracle ni funcionamiento integral del entorno portable ni HU/RN de Social.

## Cambio independiente — Teams, imagen y liderazgo — 23/10/2026

- Autorización expresa del usuario: ejecutar ampliación de Teams, esquema exacto
  `SBL_TEAM` y almacenamiento MinIO. Decisión registrada como E04 en enmiendas.md.
- Backend: `teams/entity/Team.java`, DTOs request/response, mapper y TeamImagenMapper,
  TeamService/Impl, TeamController y handler local de concurrencia; TeamImagenValidator;
  nuevo puerto público `teams/api/TeamAccesoApi` implementado en
  `seguridad/api/impl/TeamAccesoApiImpl`; perfil interno TeamPerfilDTO/obtenerPerfil.
  Seeder de desarrollo conserva creación interna sin requerir una sesión JWT.
- Campos: imagenKey, color, slogan, referenciaBiblica, textoVersiculo, idUsuarioLider,
  version. Solo administrador asigna/retira líder; edición de equipo activo por admin
  o líder con cuenta/persona/membresía activa. No se equiparan IDs usuario/persona.
- MinIO: `teams/{id}/logo/`, clave en BD y URL en respuesta; 5 MiB, MIME/extensión/firma
  JPEG/PNG/WebP. Limpieza tras commit/rollback, sin garantía distribuida ni reintento durable.
- Angular: modelos/servicio/formulario/listado Teams, nuevo `mi-equipo.ts`, ruta
  `/mi-equipo`, acceso en menú de perfil del layout y edición accesible al líder.
  Se reutilizan HttpClient, interceptores y layout; no se cambia seguridad global.
- Migración manual sin ejecutar: `docs/teams/migracion-sbl-team.sql`; copia IDs,
  columnas nuevas, identidad y generación de cambios de FK para revisión del DBA.
  Requiere aprovisionar SBL_TEAM y aplicar FK/permisos antes de usar esta versión.
  La relación JPA heredada de Persona con Team se conserva; no hay refactor de membresía.
- Verificación: compilación normal backend correcta tras corregir compatibilidad
  del constructor TeamRequest. Recompilación de todas las fuentes con POM temporal
  aislado y 15 pruebas TeamGestionTest: 15 correctas, 0 fallos/errores.
  El POM original y sus dependencias no se alteraron; temporales de verificación retirados.
  Logs: `SaludabementeERP/target/teams-tests.log` y reports en target/teams-tests.
- Angular: build development correcto; prueba team-form.spec.ts: 1 correcta.
  Primer intento de Vitest bloqueado por EPERM en temporales; reintento fuera
  del sandbox autorizado correcto. Advertencias existentes de imports Angular
  sin usar, mapper Persona/avatar y convertidor RabbitMQ obsoleto.
- Modulith: recompilación aislada de ModularityTests; 2 pruebas, 1 error en
  verifiesModularStructure por Violations, documentación modular pasó. Persisten
  ciclos actividades/asistencias y alertaclinica/nutricional/aptitudfisica y accesos
  a tipos internos de otros módulos. El reporte no contiene infracciones de Teams
  ni TeamAccesoApi. Log: `SaludabementeERP/target/teams-modularity.log`.
- Pendiente: ensayo de migración Oracle, MinIO real y comprobación visual con sesión
  autenticada. No se ejecutó SQL ni se arrancó el backend/perfil dev.
- HU/RN Social: ninguna adicional acreditada; F0–F18 no avanzan por este encargo.
  Auditoría histórica y demás propuestas de evolución siguen pendientes.


Preparación documental completada. Implementación completa solicitada posteriormente
por el usuario, siguiendo los patrones de Alertas Clínicas con Nutricional y Aptitud Física.
El resumen vigente de contratos aparece primero; debajo se conserva el historial de entregas.

**Última entrega de contratos:** ver [contratos-cerrados-v1.md](contratos-cerrados-v1.md)
y [validación de contratos](validacion-contratos.md). APIs implementadas y 54 pruebas
aisladas correctas. Esto no acredita cierre de Oracle, sincronización ni F14.

## Actualización E04 — imagen y líder de equipo — 23/10/2026

- Solicitud del usuario: incorporar en Social imagen URL, líder y los datos visuales
  implementados en Teams. Reinspección confirma TeamPerfilDTO y obtenerPerfil reales.
- TeamApiService/Impl: obtenerPerfilesPorIds con máximo 100 IDs únicos, usando la
  consulta batch del repositorio propio; no cambia TeamSnapshotDTO ni entidades/CRUD.
- SocialEquipoApi: perfil individual y batch, SocialEquipoPerfilDto y SocialLiderEquipoDto.
  Expone imagenUrl, color, slogan, referenciaBiblica, textoVersiculo e idUsuarioLider;
  identidad del líder resuelta en vivo por Seguridad, sin correo/roles ni persistencia.
- Snapshots, membresías y búsquedas sociales propagan la imagen pública de Teams
  en logoUrl/logoEquipoUrl en lugar de forzar null. Enriquecimiento de líderes por ID,
  con deduplicación y fallback; sin líder devuelve null. Los permisos de E03 no cambian.
- Fixture completo: perfil-equipo-social.json. Nuevas 9 pruebas en EquipoPerfilContratoTest.
- Verificación real: mvnw.cmd -o -Pcontratos-social test → BUILD SUCCESS,
  54 pruebas, 0 fallos/errores/omitidas. Compilación de fuentes actuales incluida.
  Log: SaludabementeERP/target/social-perfil-equipos-test.log.
- Noticias: la búsqueda de llamadas confirma que todavía no invoca SocialApiService
  ni sincronizarPostDeNoticia. Sigue pendiente F3/F16. Teams aporta perfil y membresía,
  no crea publicaciones automáticamente al crear/editar un equipo.
- No se implementaron pantallas ni endpoints REST Social: mostrar esos perfiles en
  muro/cabecera requiere el dominio/respuestas y frontend de sus fases correspondientes.
  No se arrancó backend ni se ejecutó SQL; no se revalidó el grafo global en esta entrega.

## Entrega vigente — contratos entre módulos — 23/10/2026

- Solicitud: cerrar contratos antes del dominio Social. Reglas autorizadas:
  oficialidad explícita false por defecto y admin de equipo como Coordinador
  activo vinculado a persona activa del equipo activo. Decisiones registradas en E03.
- Implementados contratos dedicados de identidad, membresías, consultas de
  actividades/noticias, almacenamiento local de adjuntos y puente tipado de avisos.
  Se conservan las APIs anteriores y sus firmas. Cinco eventos públicos del maestro.
- Noticias: oficial/resumen/idEquipoAutor/urlCompleta en entidad/request/response,
  mapper y actualización de borrador; validación de equipo autor por API Teams.
  Actividad: atribución opcional ID_EQUIPO_ACTIVIDAD; su asignación y sincronización
  siguen en el flujo dueño F15, sin deducirla del creador.
- Verificación real final: mvnw.cmd -o -Pcontratos-social clean test → BUILD SUCCESS;
  45 pruebas, 0 fallos/errores/omitidas. Compilación limpia de código actual y tests.
  Incluye J01/J02, membresías/identidad, RN34, lotes, adjuntos y bridge con mocks.
- Verificación global separada, recién compilada:
  mvnw.cmd -o -Pcontratos-social -Dtest=ModularityTests#verifiesModularStructure test
  → BUILD FAILURE: 1 prueba, 1 error Violations. Persisten ciclos de
  actividades/asistencias; alertaclinica/aptitudfisica; alertaclinica/nutricional;
  alertaclinica/nutricional/recomendacionesia/aptitudfisica; nutricional/recomendacionesia,
  además de violaciones existentes de acceso a tipos internos. No se silencia el test.
  El diagnóstico final no reporta módulo ficticio socialcontracts ni referencias a
  las nuevas implementaciones en las violaciones; no demuestra el grafo futuro de F15/F16.
- Los tests se ubican fuera del namespace raíz productivo, upeu.edu.pe.socialcontracts.
  Perfil Maven optativo contratos-social con salida aislada, sin nuevas dependencias.
- No se arrancó backend/Spring ni se ejecutó SQL. No se conectó a servicios externos.
  No se modificaron perfiles de aplicación, límites multipart ni archivos Teams.
  Se observaron cambios concurrentes de Teams y se revalidó tras su estabilización.
- HU/RN: validación parcial de contratos de identidad, RN15/16/22/34 y almacenamiento;
  ninguna HU funcional completa acreditada. J03–J19 de CRUD siguen pendientes.
- F0/Oracle/migraciones, C08 del grafo futuro, modelo y servicios Social, listeners,
  plantillas provisionadas, entrega real y reintentos F14, diez puntos F15/F16 pendientes.
- Evidencia detallada y matriz por test: validacion-contratos.md. Logs en
  SaludabementeERP/target/social-contratos-test.log y social-contratos-modulith.log;
  reportes en target/contratos-social/surefire-reports.

| Elemento | Estado observado |
|---|---|
| Maestro y plan anterior | Recuperados; conservados íntegros |
| Backend y frontend reales | Inspeccionados en el proyecto indicado por el usuario |
| LOCAL.zip original | No disponible en los adjuntos expuestos por read_thread |
| Módulo backend social | No encontrado en el árbol inspeccionado |
| Frontend social | Sin archivos ni referencias social en src durante la búsqueda |
| Seguridad social | API, DTOs e implementación existentes; inspección estática |
| Contratos restantes | Brechas documentadas; no generados ni modificados |
| Oracle / Keycloak / MinIO / RabbitMQ en ejecución | No verificados |
| F0–F18 | No se ejecutó ninguna fase; no inferir avance por clases de otros módulos |
| Compilación y pruebas de aplicación | No ejecutadas en esta tarea documental |

No se arrancó el backend: el perfil dev observado contiene ddl-auto:create. El estado de la base no se infiere del YAML ni de las entidades. No se modificaron dependencias, rutas, seguridad, controladores, entidades ni configuración.

Actualizar este archivo al cerrar cada fase con fecha, archivos, contratos usados, pruebas y resultado. Mantener pendientes diferenciados de fallos demostrados.

## Avance de implementación — 23/10/2026

- El nuevo adjunto y el maestro local son idénticos por SHA-256 (ver enmiendas.md).
- Preparación independiente de F1: SocialApiService, ActividadSocialDto,
  NoticiaSocialDto y los package-info de api y api/dto, bajo el namespace real.
- Son exactamente las cuatro operaciones y los campos de §10 del maestro.
  No hay implementación vacía, endpoints, entidades, repositorios ni llamadas
  conectadas desde productores. No hay nuevas firmas en otros módulos.
- Patrón contrastado: MotorAlertasApiService, su implementación delegada y
  PersonalClient; se mantienen llamadas directas según la indicación del usuario.
- Verificación: mvnw.cmd -o -DskipTests compile → BUILD SUCCESS, Java 21,
  792 fuentes compiladas. Advertencias ajenas a Social: avatarUrl sin mapear
  en PersonaMapper y convertidor Jackson2 de RabbitMQ obsoleto.
- HU/RN: ninguna acreditada todavía. F1 prepara contratos para HU01–03 y
  RN01–03; la idempotencia y la integración no están implementadas.
- F0 pendiente: provisión Oracle aislada (C02), resolución/verificación del grafo
  con llamadas directas (C08), declaración de dependencias (C09).
- F2–F18 pendientes. La preparación de F1 no acredita cierre de F0.
- Verificación modular: el comando normal de test no llegó a ejecutar Modulith:
  testCompile falla por incompatibilidades existentes en MisAlertasSecurityTest,
  AreaControllerTest, AreaServiceIT y PeriodoServiceIT, entre otras.
- Se ejecutó después solamente verifiesModularStructure con surefire:test,
  usando la clase de prueba previamente compilada y las clases principales recién
  compiladas. Resultado: 1 test, 1 error (Violations). Confirma ciclos existentes,
  incluidos actividades/asistencias y alertaclinica/aptitudfisica. Es diagnóstico
  acotado, no una suite recompilada ni integración Oracle. Logs en
  SaludabementeERP/target/social-modularity-check.log y social-modularity-isolated.log.
- No se ejecutó SQL, no se arrancó el backend y no se modificaron perfiles,
  seguridad, límites multipart ni dependencias Maven.

## Cambio independiente — asistencia manual agrupada — 23/10/2026

- Solicitud explícita del usuario: agrupar el JSON de asistencia por actividad
  con detalle por persona, manteniendo los IDs y la persistencia individuales.
  No corresponde a F0–F18 ni acredita HU/RN del módulo social.
- Backend: nuevos DTOs `AsistenciaManualDetalleRequest` y
  `AsistenciaActividadResponse`; extensiones en `AsistenciaController`,
  `AsistenciaService` y `AsistenciaServiceImpl`, dentro de `asistencias/registro`.
  POST `/api/v1/asistencias/manual/detalle` crea/edita marcas mixtas en una
  transacción. GET `/api/v1/asistencias/manual/actividad/{idActividad}` agrupa
  el detalle completo. Conserva los endpoints anteriores.
- Frontend: `admin/asistencias/models.ts`, `asistencia-service.ts` y
  `registro/guardar-marcacion-modal.ts`/`.html`: una solicitud para guardar la
  lista; consulta agrupada y conservación del borrador completo ante error.
- Pruebas nuevas: `AsistenciaManualDetalleTest.java` (4 pruebas) y
  `asistencia-service.spec.ts` (3 pruebas). Contrato y ejemplos en
  `docs/asistencias-manual-detalle.md`.
- Verificación real: `mvnw.cmd -o -DskipTests compile` y
  `ng.cmd build --configuration development` correctos, con advertencias de
  mapeo/avatar, conversor RabbitMQ obsoleto e imports Angular no usados.
- Backend: 4 pruebas unitarias pasaron con mocks, sin contexto Spring ni BD.
  Se usó una copia temporal del POM con testIncludes limitado al test nuevo,
  para aislar los errores históricos de testCompile; el POM original no cambió.
  El primer intento quedó bloqueado por la conexión del agente Mockito en el
  sandbox; el reintento fuera del sandbox con javaagent explícito pasó.
- Frontend: `ng.cmd test --watch=false
  --include=src/app/features/admin/asistencias/asistencia-service.spec.ts`:
  3 pruebas correctas. El primer intento falló por EPERM en temporales de
  Vitest; el reintento autorizado fuera del sandbox pasó.
- Sin cambios de entidades, esquema, configuración, autenticación o
  dependencias. No se arrancó la aplicación ni se ejecutó SQL. Pendiente:
  comprobación funcional con servicios reales y rollback transaccional Oracle;
  las pruebas con mocks no acreditan integración ni rollback de BD.


