package upeu.edu.pe.Saludabemente.asistencias.registro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.MetodoRegistro;

import java.time.LocalDateTime;

@Schema(description = "Item de historial de asistencia")
public record AsistenciaHistorialResponse(
        Long idAsistencia,
        Long idActividad,
        String nombreActividad,
        LocalDateTime fechaActividad,
        EstadoAsistencia estado,
        MetodoRegistro metodoRegistro,
        LocalDateTime horaMarcado
) {}