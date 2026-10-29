# Teams: identidad visual y liderazgo — 23/10/2026

Solicitud: ampliar Teams, usar exactamente `SBL_TEAM` y reutilizar MinIO.
Implementación independiente del plan F0–F18 de Social. No acredita HU/RN de Social.

## Comportamiento

- Equipo: nombre, descripción, activo, color hexadecimal, slogan (150), referencia
  bíblica (100), texto del versículo (1000), clave de imagen e ID de usuario líder.
- Entidad `Team` apunta a `SBL_TEAM.EQUIPO`. No se cambió el esquema global.
- Solo administrador crea/inactiva y asigna/retira líder.
- El líder debe ser cuenta activa vinculada a persona activa del equipo.
  Su permiso se revalida en cada edición, incluso si dejó de pertenecer al equipo.
- Administrador y líder vigente editan los datos del equipo activo y su imagen.
- Identidad autenticada mediante APIs existentes de Seguridad; sin acceso directo
  a Keycloak, sin equivalencia entre IDs de usuario y persona.
- `TeamAccesoApi` es un puerto de Teams implementado por Seguridad, que compone
  las APIs de identidad y membresía. Teams no importa entidades/repositorios externos
  ni añade dependencias salientes hacia Seguridad o Personal.
- `TeamApiService.obtenerPerfil` expone los campos nuevos a consumidores internos;
  los snapshots anteriores conservan su contrato. No cambia automáticamente los
  permisos de Social ni la regla existente de Coordinador en SocialEquipoApi.

## HTTP

Base `/api/v1/equipos`. GET/POST/PUT/DELETE anteriores conservan sus rutas.
POST/PUT de datos reciben nombre, descripcion, color, slogan, referenciaBiblica
y textoVersiculo. Los campos nuevos son opcionales; PUT representa reemplazo de datos.
El liderazgo y la imagen se gestionan por operaciones separadas, nunca por campos
arbitrarios enviados en el JSON del perfil.

| Operación | Acceso |
|---|---|
| GET `/mi-equipo` | Equipo activo de la persona del usuario actual |
| GET `/mi-equipo/miembros` | Nombres y cuentas activas de su propio equipo |
| GET `/{id}/puede-editar` | Booleano calculado para usuario actual |
| GET `/{id}/candidatos-lider` | Administrador |
| PUT `/{id}/lider` con `{"idUsuario":42}` | Administrador; null retira líder |
| POST `/{id}/imagen`, multipart parte `archivo` | Administrador o líder vigente |

## MinIO y consistencia

Se reutilizan AlmacenamientoImagenService y MinioService, bucket/configuración actuales.
Prefijo `teams/{id}/logo/`; UUID y extensión del archivo validada. Oracle almacena
la clave; la API devuelve `imagenUrl` construida por el servidor. Admite JPEG, PNG,
WebP, máximo 5 MiB; valida MIME, extensión y firma binaria, sin decodificación completa.
No se cambiaron multipart global, dependencias ni políticas del bucket.

Al confirmar la transacción Oracle se elimina la imagen anterior; al rollback se
intenta eliminar la nueva. Usa el borrado existente de MinioService, que registra
fallos sin reintentarlos de forma durable. Una caída del proceso o fallo de borrado
puede dejar un objeto huérfano: no se afirma atomicidad distribuida.
`@Version` detecta escrituras concurrentes de la entidad durante transacciones;
conflicto retorna 409 local a Teams. No es control de versiones de un formulario
abierto durante largo tiempo ni historial de cambios.

## Angular

Formulario existente de Teams ampliado: campos, selector de color, archivo y
previsualización. Se crea/actualiza el equipo antes de subir su imagen; un error
de upload conserva el ID para reintentar sin duplicar el equipo en esa pantalla.
La sección de liderazgo del administrador muestra candidatos del equipo.
Para equipos nuevos: guardar, asignar integrantes desde Personal, editar y asignar líder.
Listado muestra imagen, color y slogan. `/mi-equipo` muestra identidad, integrantes
con cuenta activa y marca al líder; enlace de edición solo si backend lo permite.
Acceso desde el menú del perfil del layout existente. La ruta de edición exige sesión
y permite llegar al líder; el backend verifica toda mutación.

## Migración y despliegue

### Instalación nueva con Hibernate create

Archivo único: `../../SCRIPTS/S00_01_preparar_backend.sql`.
Ejecutar como SYSTEM en FREEPDB1, usando F5 en SQL Developer.
SALUDABLEMENTE_APP debe existir previamente.

Por corrección expresa del usuario, el formato reproduce su script original:
primero `GRANT DBA TO SALUDABLEMENTE_APP;`, después 14 bloques CREATE USER
con clave de desarrollo `123456`, cuota en USERS, permisos CREATE SESSION,
TABLE, VIEW, SEQUENCE, TRIGGER, PROCEDURE y UNLIMITED TABLESPACE.
Incluye SBL_TEAM, SLB_RECOMENDACIONES_IA y SLB_SOCIAL.
No contiene PL/SQL dinámico, creación de la cuenta de aplicación ni detección
de tablespaces. Requiere USERS; si los esquemas ya existen, CREATE USER falla.
Las tablas las crea Hibernate mediante el ddl-auto:create existente.
No se ejecutó el script ni se arrancó el backend.

### Traslado de datos de una instalación existente

`migracion-sbl-team.sql` es un artefacto manual, NO ejecutado ni validado en Oracle.
Requiere esquema SBL_TEAM provisionado por DBA, cuota/tablespace y usuario de aplicación
del entorno. No incluye credenciales. Copia los IDs históricos, comprueba la copia,
adelanta la identidad y genera para revisión los cambios de FK que apuntan al origen.
Las FK generadas deben ejecutarse en el corte antes de reabrir escrituras.

Personal conserva su relación JPA heredada hacia Team; este trabajo no refactoriza
la membresía existente ni añade relaciones entre módulos. Deben migrarse sus FK
y las de cualquier consumidor detectado en Oracle. No basta con cambiar @Table.
Revisar también vistas, SQL nativo, triggers y jobs del entorno. No se elimina la
tabla original. DDL Oracle hace commits implícitos: no confiar en rollback SQL
para revertir el corte. Ensayar y verificar respaldo/restauración en copia aislada.
No arrancar el perfil dev con ddl-auto:create contra datos existentes.

## Pendientes de evolución

Auditoría histórica, historial de liderazgo, sublíderes, portadas, objetivos y limpieza
durable de objetos huérfanos son propuestas futuras, no prestaciones implementadas.
Integración real Oracle/MinIO y comprobación visual con sesión autenticada pendientes.

## Verificación ejecutada

- `mvnw.cmd -o -DskipTests compile`: correcto.
- POM temporal derivado del perfil existente contratos-social, limitado a tests de
  Teams y salida target/teams-tests: recompilación completa y 15 pruebas correctas.
  No modifica el POM original. Log: target/teams-tests.log.
- `ng.cmd build --configuration development`: correcto, con advertencias existentes.
- `ng.cmd test --watch=false --include=src/app/features/admin/teams/team-form.spec.ts`:
  1 prueba correcta tras reintento autorizado fuera del sandbox por EPERM de temporales.
- ModularityTests recompilado y ejecutado aislado: 2 pruebas, 1 error en verificación
  por ciclos/accesos internos existentes; no aparecen infracciones Teams/TeamAccesoApi.
  Log: target/teams-modularity.log. No se declara verificación modular global correcta.


