# Inventario del código inspeccionado

Rutas relativas a la raíz LOCAL indicada en README.md. Evidencia estática; no equivale a pruebas ejecutadas.

| Evidencia | Hallazgo |
|---|---|
| SaludabementeERP/pom.xml | Java 21; Spring Boot 4.1.1; Spring Modulith 2.1.1 |
| SaludabementeERP/src/main/java/upeu/edu/pe/Saludabemente | Namespace real; respetar su escritura existente |
| …/seguridad/api/social/SocialUsuarioApi.java | Los seis métodos previstos por seguridad v1.2 existen |
| …/seguridad/api/impl/SocialUsuarioApiImpl.java | Resuelve autenticado por JWT/email; rechaza inactivos con ResourceNotFoundException; batch limitado a 100 |
| …/personal/api | APIs para otros consumidores; no se encontró personal/api/social |
| …/teams/api/TeamApiService.java | Módulo de equipos existente; no sustituye automáticamente al contrato SocialEquipoApi |
| …/actividades/api/ActividadApiService.java | Consulta por snapshot, rango, batch y otras operaciones; no es el contrato social propuesto |
| …/noticias/api/NoticiaQueryApi.java | Consulta de noticias publicadas |
| …/notificaciones/api/NotificacionApi.java | enviar con destinatario idPersona y variables de plantilla |
| …/shared/package-info.java | Módulo OPEN |
| …/shared/storage/AlmacenamientoImagenService.java | guardar valida y delega; no método público validar |
| …/shared/storage/MinioService.java | guardar retorna nombre de objeto; construirUrlPublica genera URL aparte |
| …/exception/GlobalExceptionHandler.java | ResourceNotFoundException → 404; BusinessRuleException → 409 |
| SaludabementeERP/src/main/resources/application-dev.yml | ddl-auto:create; default_schema:SLB_PERSONAL; multipart 15MB / 20MB |
| SaludabementeERP/src/test/java/upeu/edu/pe/Saludabemente/ModularityTests.java | modules.verify() y generación de documentación; no ejecutados |
| saludablemente-frontend/package.json | Angular ^22.1.0; Taiga UI ^5.26.0; RxJS ~7.8.0; TypeScript ~6.0.2; Vitest ^4.0.8 |
| saludablemente-frontend/src/app/app.routes.ts | Layout autenticado, AuthGuard y RoleGuard; componentes con carga diferida |
| saludablemente-frontend/src/app/app.config.ts | Zoneless; HttpClient con interceptores de token Keycloak, trace y loading; Taiga; ECharts |
| saludablemente-frontend/src/environments/environment.ts | Configuración de entorno existente; reutilizar al programar |

No se encontraron AGENTS.md en LOCAL ni en los ancestros consultados hasta D:\. No se hicieron cambios a instrucciones preexistentes. No se copiaron configuraciones privadas al paquete.