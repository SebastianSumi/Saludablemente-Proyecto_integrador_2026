# Estrategia de verificación futura

No se ejecutaron pruebas de aplicación en esta entrega documental. El flujo original completo está en pruebas-maestro.md. No arrancar dev con ddl-auto:create sobre la base existente.

| Área | Casos mínimos derivados del maestro |
|---|---|
| Sincronización | RN01–03: duplicados y concurrencia por tipo/ref; cambios de estado e imágenes; cancelada visible RN07 |
| Visibilidad | RN08/14/34–39: inactivos, auto-generados no editables, scope correcto, autor real de share |
| Comentarios | RN04/06/12/13: propietario/admin, raíz y respuesta, rechazar nietos y padre de otro post, cascada y tombstone acordado |
| Reacciones | RN05/09–11: una reacción por target/usuario, reemplazo y borrado físico; concurrencia |
| Equipos | RN15/16: miembro/no miembro, autor original y admins; IDs de persona/usuario diferenciados |
| Shares | RN17–19/37/38: resolver root, original inactivo, contador y duplicado según C15 |
| Pins | RN20/33/36: cuarto 409, scope GLOBAL con null en Oracle, concurrencia, expiración y oficial |
| Texto | RN21–23/40: normalización, límites, menciones inexistentes, reparseo y post solo imagen según C10 |
| Moderación | RN24–26: XOR targets, segundo reporte 409, aceptar/rechazar y restaurar con permisos |
| Alcance | RN27–31: primera/repetida, autor/admin excluidos, solo detalle, concurrencia, purga sin recalcular total y C12 |
| Privacidad | RN31/32: alcance solo autor/admin; guardados inaccesibles a terceros |
| Adjuntos | JPEG/PNG/WebP, >5 MB, quinto archivo, claves ajenas, no duplicar URLs existentes; semántica C05 |
| Eventos | Cinco tipos, destinatarios correctos, sin autoaviso, commit/rollback; degradación de equipo declarada |
| Angular | Feed y paginación, permisos de botones, errores 404/409, estados vacíos, teclado, upload, perfil, moderación y enlaces |
| Modulith | modules.verify real; API-only imports, ausencia de ciclos; probar grafo completo, no solo imports |

Ejecución futura en backend: usar wrapper Maven de Windows y entorno de pruebas aislado tras revisar application-test.yml. `mvnw.cmd test -Dtest=ModularityTests` ejecuta la verificación existente; no afirmar verde sin guardar resultado. Build y suite según fase y dependencias disponibles.

Frontend: scripts existentes `npm run build` y `npm test` (configurar modo no interactivo conforme al runner real). No instalar versiones nuevas ni cambiar lockfile para esta documentación.

Cobertura: cada HU y RN conserva su ID en requerimientos.md; añadir resultados concretos por ID al implementar. Los casos anteriores son plan de pruebas, no evidencia de cumplimiento.