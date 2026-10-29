# Backend de etiquetas y asociaciones clínicas

## Alcance

Actualización de colaborador: se añadieron `GET /api/v1/mis-alertas` y `GET /api/v1/mis-alertas/{id}` con pertenencia resuelta en servidor. Las rutas globales y de administración ahora se restringen por rol. Ver `../saludablemente-frontend/COLABORADOR_ALERTAS_CLINICAS.md` para contratos, permisos y verificación.

Se conserva la arquitectura Controller → Service/ServiceImpl → Repository, los DTO y mappers del módulo. EtiquetaParametro y AlertaEtiqueta siguen siendo entidades puente con fotocopias. No se crea ningún endpoint para generar alertas manualmente, ni se modifican los cálculos clínicos. No se requiere migración de tablas.

## Contrato de guardado

Cada PUT de asociaciones recibe el conjunto final completo de la tarjeta, no únicamente las filas nuevas. Primero consultar el GET correspondiente y conservar `revision`. Enviar las asociaciones MOTOR existentes: no se permite retirarlas. `detalles: []` retira todas las asociaciones editables y falla si implicaría retirar una MOTOR. Omitir `detalles` o enviar null es un error 400. Máximo 500 filas por operación.

```json
{
  "revision": "copiar-exactamente-la-revision-del-GET",
  "detalles": [
    { "idEtiqueta": 1 },
    { "idEtiqueta": 2 }
  ]
}
```

Se envían exclusivamente IDs en cada detalle. Nombre, código, unidad, color, origen y fechas los determina el servidor. Cada operación es una única transacción. Se preservan las fotocopias y fechas de las asociaciones que permanecen.

| Tarjeta | GET para cargar y obtener revision | PUT para guardar | Campo de cada detalle |
|---|---|---|---|
| Etiquetas de alerta | `/api/v1/alertas/{id}/etiquetas/edicion` | `/api/v1/alertas/{id}/etiquetas` | `idEtiqueta` |
| Alertas de etiqueta | `/api/v1/etiquetas-clinicas/{id}/alertas` | Misma URL | `idAlerta` |
| Parámetros de etiqueta | `/api/v1/etiquetas-clinicas/{id}/parametros` | Misma URL | `idParametro` |
| Etiquetas de parámetro | `/api/v1/parametros-evaluacion/{id}/etiquetas` | Misma URL | `idEtiqueta` |

Cada respuesta de tarjeta devuelve su identificador de cabecera, `revision` y `detalles` con datos completos. Reemplazar el estado local con la respuesta del PUT, incluida la nueva revisión.

El GET antiguo `/api/v1/alertas/{id}/etiquetas` conserva su respuesta de arreglo para compatibilidad. Los POST/DELETE individuales siguen disponibles y ahora respetan la protección de asignaciones automáticas.

## Crear y editar etiquetas

POST `/api/v1/etiquetas-clinicas`:

```json
{
  "codigo": "SEGUIMIENTO_NUTRICIONAL",
  "nombre": "Seguimiento nutricional",
  "descripcion": "Clasificación para seguimiento",
  "colorHex": "#2E7D5B",
  "detalles": [
    { "idParametro": 1 },
    { "idParametro": 2 }
  ]
}
```

PUT `/api/v1/etiquetas-clinicas/{id}` usa la misma estructura más `revision`, obtenida del GET de la etiqueta completa. Esta revisión es distinta de la revisión de una tarjeta independiente; no intercambiarlas. El código no puede cambiar. `detalles: []` permite una etiqueta de uso manual sin parámetros.

**Cambio de contrato:** `idsParametros` fue sustituido por `detalles: [{"idParametro": ...}]`. Actualizar cualquier cliente REST o consumidor que utilizara el campo anterior. Las respuestas de etiqueta conservan `parametros` y `alertas`, y añaden `revision`.

Las acciones existentes DELETE `/{id}` y PATCH `/{id}/reactivar` conservan la baja lógica y reactivación. Desactivar no elimina asociaciones históricas ni recalcula alertas previas.

## Consultas para frontend

- GET `/api/v1/etiquetas-clinicas/consulta`: `estado`, `texto`, `idParametro`, `pagina` (desde 0), `tamanio` (1–100, por defecto 20). Devuelve Page con `content`, `totalElements`, etc. Cada fila tiene código, nombre, color, estado y cantidades reales de parámetros y alertas. Paginación en base de datos.
- GET `/api/v1/alertas/opciones`: `personaId`, `estado`, `severidad`, `desde`, `hasta`, `pagina`, `tamanio`. Resumen paginado de alertas existentes para seleccionar asociaciones. Los nombres de personas se resuelven con el mecanismo de personas que ya utiliza el frontend; la respuesta contiene `personaId`.
- GET `/api/v1/parametros-evaluacion`: se mantiene el catálogo existente con código, nombre y unidad. Filtra activos por defecto.
- Las consultas originales de alertas conservan los filtros por etiquetas y demás campos.

## Reporte

GET `/api/v1/etiquetas-clinicas/reporte`:

- `estadoEtiqueta`: ACTIVO o INACTIVO.
- `idEtiqueta`, `idParametro`.
- `estadoAlerta`: PENDIENTE, ATENDIDA o ANULADA.
- `severidad`: LEVE, MODERADA o CRITICA.
- `desde`, `hasta`: fechas ISO de **asignación**, inclusivas.

Devuelve cantidad de etiquetas, activas dentro del filtro, asociaciones, alertas únicas y críticas únicas, además de filas con parámetros, alertas y última asignación. Una alerta con dos etiquetas cuenta como dos asociaciones y una alerta única. Incluye etiquetas sin asignaciones coincidentes con contadores cero. El reporte completo no está paginado; los selectores sí lo están.

Los reportes históricos `/api/v1/alertas/resumen/etiquetas` mantienen sus fotocopias. El nuevo reporte agrupa por ID del catálogo y muestra sus datos actuales, con sus asignaciones históricas embebidas.

## Reglas y concurrencia

- 400: JSON, detalles, identificadores o parámetros de consulta inválidos; revisión ausente al editar.
- 404: alerta, etiqueta o parámetro inexistente.
- 409: duplicados, asociaciones MOTOR protegidas, nuevas asociaciones inactivas, revisión desactualizada o conflicto de bloqueo.
- No se devuelven entidades JPA como JSON.
- Todos los recursos mantienen la autenticación actual del proyecto; CORS ya admite GET/POST/PUT/PATCH/DELETE.

La revisión es una huella del estado consultado, sin nuevas columnas. Se valida bajo bloqueo de escritura compartido por las dos direcciones, los endpoints individuales, el mantenimiento de etiquetas y el motor. Para el catálogo clínico pequeño se bloquean sus cabeceras en orden fijo (parámetros y después etiquetas). Esto prioriza consistencia y serializa las escrituras de asociaciones; si el catálogo crece mucho deberá sustituirse por un protocolo de bloqueo más granular.

El motor solo incorpora este protocolo de bloqueo; conserva su generación automática y sus reglas de cálculo. El controlador no acepta origen de asignación ni fechas del cliente.

## Archivos y función

- `etiqueta/dto/*DetalleRequest`: identificadores validados de cada línea.
- `etiqueta/dto/Guardar*Request`: revisión y colección completa de detalles.
- `etiqueta/dto/*Response`: cabeceras y detalles de cada tarjeta y reporte.
- `EtiquetaClinicaRequestDto`, `EtiquetaClinicaResponseDto`, `EtiquetaClinicaMapper`: nuevo detalle compuesto y revisión de edición de etiqueta.
- `AsociacionClinicaService` y `AsociacionClinicaServiceImpl`: validación, diferencias de colecciones y guardado atómico en las cuatro direcciones.
- `RevisionAsociaciones`: generación y validación de revisión.
- `BloqueoAsociaciones`: protocolo de bloqueo común.
- `ConsultaEtiquetaService` y su implementación: listados resumidos y reporte con agregados.
- `EtiquetaClinicaServiceImpl`: adaptación del mantenimiento existente, rechazo de duplicados y revisión antes de editar.
- `EtiquetaClinicaRepository`, `AlertaEtiquetaRepository`, `AlertaClinicaRepository`: proyecciones, consultas paginadas y lectura de asociaciones para reporte.
- `EtiquetaClinicaController`, `AlertaClinicaController`, `ParametroEvaluacionController`: endpoints documentados en Swagger.
- `AlertaClinicaService` y su implementación: selector paginado, validación de filtros y protección MOTOR también en la operación individual.
- `GeneradorAlertaService`: coordinación del bloqueo con las operaciones de asociaciones.
- `GlobalExceptionHandler`: conflictos de bloqueo y parámetros de consulta con respuestas coherentes.
- `pom.xml`: perfil opcional para compilar y ejecutar exclusivamente las pruebas nuevas, independiente de los tests antiguos de catálogo.

## Verificación

```text
mvn -o -q -DskipTests compile
mvn -o -q -Ppruebas-alertas-clinicas test
```

Las pruebas nuevas usan H2 aislado, transacciones reales y controladores HTTP. Verifican ambas direcciones, guardado múltiple, rollback después de flush, protección MOTOR, revisiones, duplicados, inactividad, preservación histórica, conteos únicos, validación de cada detalle y esquema de Swagger.

Resultado verificado: **16 pruebas, 0 fallos y 0 errores** (12 de integración y 4 de contrato). Incluye dos escrituras simultáneas con la misma revisión: una confirma y la otra se rechaza por conflicto.

La ejecución general `mvn test` encuentra incompatibilidades anteriores en pruebas de Área y Período con sus DTO actuales. El perfil de este cambio no altera esas pruebas ni la ejecución predeterminada. H2 verifica la lógica y la persistencia; no sustituye una prueba final contra Oracle y la autenticación del entorno real.
