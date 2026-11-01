# Asistencia manual agrupada — 23/10/2026

La cabecera es una agrupación JSON por actividad. No se agrega tabla ni ID de
cabecera: cada persona conserva su `idAsistencia` y la unicidad existente por
actividad, persona y tipo de asistencia.

## Registrar y editar

`POST /api/v1/asistencias/manual/detalle`

```json
{
  "idActividad": 8,
  "tipoAsistencia": "SIMPLE",
  "latitud": -12.0464,
  "longitud": -77.0428,
  "detalle": [
    { "idPersona": 20, "marca": "PRESENTE" },
    { "idPersona": 21, "marca": "TARDE" },
    { "idPersona": 22, "marca": "FALTA" }
  ]
}
```

Registra si no existe y actualiza si existe, conservando el ID. El tipo admite
`SIMPLE`, `CHECK_IN` y `CHECK_OUT`. Solo procesa las personas enviadas; omitir
una persona no elimina ni modifica su asistencia. No acepta personas repetidas,
detalle vacío, personas inactivas o no inscritas ni actividades fuera de
`EN_CURSO`. Reutiliza las reglas de marcación y geocerca existentes.

Una única transacción engloba todos los cambios: ante un error se revierte
el envío. El frontend conserva el borrador completo para corregirlo/reintentar.
La respuesta 200 contiene el detalle completo de la actividad, incluidos los
registros previos y todos sus tipos, con el mismo formato que la consulta.

## Consulta

`GET /api/v1/asistencias/manual/actividad/8`

```json
{
  "idActividad": 8,
  "nombreActividad": "Actividad de bienestar",
  "detalle": [
    {
      "idAsistencia": 101,
      "idActividad": 8,
      "idPersona": 20,
      "tipoAsistencia": "SIMPLE",
      "estado": "ASISTIO",
      "metodoRegistro": "MANUAL"
    }
  ]
}
```

Ejemplo abreviado: cada elemento incluye todos los campos de
`AsistenciaResponse` (horas, coordenadas, auditoría y sincronización).
`detalle` reúne las asistencias de la actividad, también las registradas por QR.
Actividad existente sin registros devuelve `detalle: []`; inexistente devuelve
404. La consulta permite revisar actividades finalizadas.

Los endpoints anteriores permanecen compatibles. El formulario de lista usa
una sola solicitud para cualquier combinación de marcas nuevas y editadas.
Las vistas existentes obtienen su lista desde `detalle` de la nueva consulta.

## Verificación

- Compilación backend y build Angular development: correctos.
- `AsistenciaManualDetalleTest`: 4 pruebas unitarias correctas (duplicados,
  actividad finalizada, marcas mixtas y conservación del ID, consulta vacía).
- `asistencia-service.spec.ts`: 3 pruebas correctas (solicitud única, consulta
  agrupada con IDs y propagación de error).
- No se ejecutó integración Oracle ni se probó rollback sobre una BD real.
  El guardado atómico está declarado con `@Transactional`; los tests usan mocks.
