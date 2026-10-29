package upeu.edu.pe.Saludabemente.asistencias.registro.dto.response;

import java.util.List;

/** Agrupación de consulta: cada detalle conserva su idAsistencia. */
public record AsistenciaActividadResponse(
        Long idActividad,
        String nombreActividad,
        List<AsistenciaResponse> detalle
) {}
