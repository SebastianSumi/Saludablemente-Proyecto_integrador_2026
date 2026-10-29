> Transcripción del Documento Maestro v1.0, 23/10/2026. Describe el objetivo; no certifica implementación. Contrastar con discrepancias.md y estado.md. Los bloques Java/SQL son referencia documental y no se han aplicado.

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

