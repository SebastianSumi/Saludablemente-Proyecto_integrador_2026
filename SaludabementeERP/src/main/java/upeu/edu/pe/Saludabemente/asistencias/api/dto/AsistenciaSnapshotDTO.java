package upeu.edu.pe.Saludabemente.asistencias.api.dto;

import java.time.LocalDateTime;

public record AsistenciaSnapshotDTO(
        Long idAsistencia,
        Long idActividad,
        Long idPersona,
        String tipoAsistencia,
        String estado,
        LocalDateTime horaMarcado,
        String metodoRegistro
) {}