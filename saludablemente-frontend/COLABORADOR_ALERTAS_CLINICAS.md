# Etiquetas clínicas en la vista de colaborador

## Funcionalidad

En `/colaborador/mis-alertas` se muestran las etiquetas en tarjetas y en la tabla. El filtro se construye exclusivamente con las etiquetas que aparecen en las alertas propias recibidas, sin consultar el catálogo administrativo. La búsqueda de texto incluye el nombre y código histórico de las etiquetas.

En `/colaborador/mis-alertas/:id` se añadió una card de consulta con nombre, color, origen y fecha de asignación. El origen manual se presenta como «Asignada por el equipo». Se mantiene el modo de solo lectura, sin acciones de asignar, retirar, editar, atender o anular.

La consulta no modifica alertas ni genera nuevas. Los datos históricos permanecen tal como devuelve el backend.

## Backend y permisos

Se añadieron dos recursos de solo lectura:

| Método y ruta | Función |
|---|---|
| `GET /api/v1/mis-alertas` | Lista las alertas de la persona identificada desde la sesión |
| `GET /api/v1/mis-alertas/{id}` | Devuelve únicamente una alerta de esa persona, con indicadores y etiquetas |

El servidor usa `SeguridadService.obtenerMiPerfil()` para resolver la persona. No acepta un `personaId` del cliente como identidad. La consulta de detalle filtra por ID y persona en el repositorio antes de mapear o devolver datos. Una alerta ajena o inexistente devuelve el mismo 404. Un usuario sin persona vinculada o sin cuenta activa recibe 403, sin ejecutar una consulta global.

Se reforzaron las rutas existentes:

| Operación | Permiso |
|---|---|
| Consultar recursos propios | Usuario autenticado y persona activa vinculada |
| Consultar bandeja global y reportes de alertas | Administrador, Evaluador o Coordinador |
| Atender una alerta | Administrador o Evaluador |
| Anular alertas o modificar asociaciones | Administrador |
| Catálogo de etiquetas y parámetros | Administrador |

La ruta `mis-alertas` rechaza métodos de escritura incluso para Administrador. Se corrigió la extracción de roles JWT para leer la colección anidada `realm_access.roles` de Keycloak. Se conserva la compatibilidad con la clave literal anterior cuando no existe el objeto `realm_access`.

## Archivos nuevos

### Frontend

- `src/app/shared/models/etiqueta-clinica-asignada.ts`: tipo compartido de la fotocopia de una etiqueta.
- `src/app/shared/components/etiquetas-clinicas/etiquetas-clinicas.ts`: presentación compartida compacta o detallada, sin edición. Los enlaces son opcionales y solo se habilitan desde la vista administrativa.
- `etiquetas-clinicas.spec.ts`: comprueba presentación, origen, fecha, estado vacío y ausencia de acciones administrativas.
- `src/app/features/colaborador/alerta/alerta-service.spec.ts`: comprueba los endpoints propios, ausencia de personaId y que no se usa un endpoint global como alternativa ante 404.

### Backend

- `alertaclinica/colaborador/controller/MisAlertasController.java`: endpoints propios, documentados en Swagger.
- `alertaclinica/colaborador/service/MisAlertasService.java` y `MisAlertasServiceImpl.java`: identidad desde sesión, validación de perfil y consulta propia.
- `alertaclinica/colaborador/MisAlertasSecurityTest.java`: pruebas con la cadena real de Spring Security y conversión JWT real, usando un decoder y repositorios simulados.

## Archivos existentes modificados

- Colaborador `alerta-service.ts`: usa directamente `/api/v1/mis-alertas`; elimina la dependencia del servicio administrativo y de una identidad obtenida en el navegador.
- Colaborador `alerta-list.ts/html/css`: etiquetas, filtro local por código, búsqueda ampliada y presentación en las dos vistas. Cancela peticiones anteriores y suscripciones al destruir el componente.
- Colaborador `alerta-detail.ts/html/css`: card de etiquetas, mensajes de acceso, reintento y limpieza de datos anteriores. Reacciona al cambio de ID en la misma ruta.
- `alerta-list.spec.ts`, `alerta-detail.spec.ts`: pruebas del filtro, búsqueda, presentación y ausencia de datos anteriores al cambiar a una alerta inaccesible.
- Administrador `etiqueta.model.ts`: reexporta el tipo compartido sin duplicarlo.
- Administrador `alerta-clinica-list.ts/html`: reutiliza la presentación compartida manteniendo los enlaces del administrador.
- Backend `AlertaClinicaRepository.java`: consulta `findByIdAlertaAndPersonaId` con indicadores y etiquetas.
- Backend `SecurityConfig.java`: permisos y lectura de roles anidados de Keycloak.
- Backend `GlobalExceptionHandler.java`: respuesta 403 consistente.
- Backend `pom.xml`: incluye las nuevas pruebas en el perfil opcional existente.
- Backend `AsociacionClinicaIntegrationTest.java`: comprueba la pertenencia en base de datos con H2.
- `tools/alertas-ui-preview/main.ts`: escenarios de colaborador con datos sintéticos para revisar la UI. No pertenece al bootstrap productivo ni conecta con el backend real.

## Verificación

Backend:

```text
mvn -o -q -Ppruebas-alertas-clinicas test
```

Las pruebas verifican autenticación, pertenencia, manipulación de personaId, acceso denegado a catálogos y bandeja global, rechazo de modificaciones, protección de rutas propias de solo lectura y permisos de Administrador/Evaluador con roles anidados.

Resultado: **26 pruebas de backend aprobadas**, incluidas las pruebas previas de asociaciones.

Frontend:

```text
node node_modules/@angular/cli/bin/ng.js build --configuration production
node node_modules/@angular/cli/bin/ng.js test --watch=false --include="**/colaborador/alerta/*.spec.ts" --include="**/etiquetas-clinicas/*.spec.ts" --include="**/alertaclinica/alerta/*.spec.ts" --include="**/alertaclinica/dashboard/*.spec.ts" --include="**/etiquetas/*.spec.ts"
```

La compilación de producción se completó. Se revisaron tarjetas, filtro, tabla, detalle y regreso a Mis alertas con datos sintéticos. La tabla conserva su desplazamiento horizontal interno en móvil; la página y el detalle no desbordan a 390 px. No se observaron errores de consola en el recorrido visual.

Resultado: **38 pruebas de frontend aprobadas**, incluidas las regresiones de la vista administrativa y del dashboard.

Capturas:

- `docs/alertas-ui/colaborador-alertas-escritorio.jpg`
- `docs/alertas-ui/colaborador-detalle-escritorio.jpg`
- `docs/alertas-ui/colaborador-detalle-movil.jpg`

Los tests de backend usan JWT simulados y H2 para persistencia. Queda pendiente la comprobación con Keycloak, Oracle y usuarios reales del entorno. Se deben ejecutar juntos el backend actualizado y este frontend, porque el servicio de colaborador ahora consume los endpoints propios.
