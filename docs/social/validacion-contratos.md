# Validación de contratos — 23/10/2026

## Actualización E04

Tras incorporar perfil visual de Teams y líder en vivo:
`mvnw.cmd -o -Pcontratos-social test` → BUILD SUCCESS, **54 pruebas, 0 fallos,
0 errores, 0 omitidas**, incluyendo compilación de las fuentes actuales.
Las nueve nuevas pruebas de EquipoPerfilContratoTest verifican el fixture
perfil-equipo-social.json, ausencia/inactividad del líder, listas en orden diferente,
deduplicación, máximo 100, imagen en snapshots/membresías/búsquedas, proveedor Teams
y ausencia de permisos implícitos por aparecer como líder.
Log: SaludabementeERP/target/social-perfil-equipos-test.log.
No prueba render del frontend ni reejecuta el grafo global de Modulith.

## Entrega inicial de contratos

Entrega solicitada: cerrar contratos con módulos existentes antes de implementar
el dominio Social. Referencia: [contratos-cerrados-v1.md](contratos-cerrados-v1.md),
E03 en enmiendas.md y reglas autorizadas de oficialidad/admin de equipo.

## Resultado comprobado

```powershell
# Desde SaludabementeERP
.\mvnw.cmd -o -Pcontratos-social clean test
```

BUILD SUCCESS. **45 pruebas, 0 fallos, 0 errores, 0 omitidas.** Recompilación limpia
de fuentes principales, pruebas de contrato y ModularityTests. Este último se compila
pero solo se ejecuta con su selección explícita; el perfil normal ejecuta los contratos.

| Grupo | Pruebas | Evidencia funcional del contrato |
|---|---:|---|
| IdentidadEquipoContratoTest | 10 | idUsuario distinto de idPersona; coordinador miembro/admin; no miembro/cuenta inactiva; equipo inactivo; destinatarios en lotes; límites; autores batch con fallback; traducción tipada 409 |
| ContenidoContratoTest | 6 | Optional por ausencia; búsqueda capeada y CANCELADA; galería sin re-subida; equipo explícito; visibilidad/noticia/oficialidad; mapeo y default false |
| AlmacenamientoContratoTest | 15 | JPEG/PNG/WebP; vacío/MIME/extensión; límite exacto 5 MiB y +1 byte; clave/URL; cuarto/quinto; duplicados; traversal/codificación/query/fragmento/otro post/autoridad |
| NotificacionContratoTest | 7 | Cinco códigos, idPersona destinatario real, variables sin email, payload incompleto y propagación del fallo de transporte |
| JsonMaestroContratoTest | 4 | Deserialización real de J01/J02; cinco eventos; lista inmutable; correspondencia de cinco plantillas y códigos |
| PersonaEquipoContratoTest | 2 | Membresía por persona/equipo activos; personas y conteos desde repositorio propio |
| LimitesContratoTest | 1 | Social no importa repositorios/entidades de módulos externos |

Las pruebas unitarias mockean las APIs/repositorios/almacenamiento/transporte.
No prueban consultas contra Oracle, objetos reales MinIO ni entrega RabbitMQ.
J03–J19 no se declaran aprobados como endpoints: el dominio/REST Social sigue pendiente.

Archivos principales nuevos: APIs y DTOs en seguridad/api/social,
personal/api/social, actividades/api/social, noticias/api/social,
notificaciones/api/social y shared/storage/social; implementaciones en los módulos
dueños/composición de Seguridad; cinco eventos y enums públicos de Social;
SocialUsuarioClientService; siete clases de test en upeu.edu.pe.socialcontracts.
Se extendieron campos explícitos de Noticias y atribución opcional de equipo en
Actividad, sin ejecutar migración ni conectar los productores a Social.

## Comprobación modular separada

```powershell
.\mvnw.cmd -o -Pcontratos-social '-Dtest=ModularityTests#verifiesModularStructure' test
```

Resultado final: BUILD FAILURE, 1 prueba, 1 error Violations. Persisten ciclos de
actividades/asistencias, alertaclinica/aptitudfisica, alertaclinica/nutricional,
alertaclinica/nutricional/recomendacionesia/aptitudfisica y nutricional/recomendacionesia,
y accesos existentes a tipos internos. No se declara Modulith verde.
El diagnóstico final no enumera las nuevas implementaciones contractuales en sus
violaciones, pero no acredita el futuro grafo al conectar F15/F16.
Se ejecuta la clase recién compilada, no un binario antiguo. Los tests están fuera del namespace
raíz de la aplicación para que no se descubra un módulo productivo ficticio socialcontracts.
No se ignoran los ciclos ni se modifica modules.verify().

## Condiciones pendientes para la implementación

- F0/C02: entorno y provisión Oracle aislados; migraciones revisadas de las columnas nuevas.
- C08/C09: grafo y declaración de dependencias completos al conectar productores/adaptadores.
- F3/F15/F16: idempotencia real, política transaccional y diez puntos de Actividades;
  asignación explícita de idEquipoActividad en su flujo dueño.
- F6: REST multipart, validación de pertenencia/existencia según flujo y CRUD de comentarios.
- F14: listeners AFTER_COMMIT, plantillas provisionadas, enlaces, reintentos/deduplicación
  y verificación de entrega real. Los eventos y el puente compilados no acreditan HU33.
- HU/RN: verificación parcial de contratos para RN15/16/22/34 y batch; ninguna historia
  completa ni alcance B01–B11 completo se acredita solo con esta suite.

## Incidencias de verificación corregidas

La primera compilación principal pasó. Dos intentos de la suite coincidieron con
ediciones simultáneas de Teams (mapper de imagen y constructor/seeder), sin cambios
de Social sobre esos archivos. Tras quedar consistentes, la compilación limpia pasó.
El primer diagnóstico modular también descubrió el paquete inicial de tests como
módulo; se movió fuera de la raíz y se limpiaron sus clases generadas antes de repetir.

No se arrancó Spring/backend, no se ejecutó SQL, no se conectó a BD/servicios externos,
no se tocaron perfiles de aplicación, autenticación global ni límites multipart.
El POM solo añade el perfil optativo contratos-social, sin nuevas dependencias;
no cambia el conjunto de pruebas global por defecto ni el perfil de Alertas existente.

Logs: SaludabementeERP/target/social-contratos-test.log y social-contratos-modulith.log.
Reportes: SaludabementeERP/target/contratos-social/surefire-reports.
