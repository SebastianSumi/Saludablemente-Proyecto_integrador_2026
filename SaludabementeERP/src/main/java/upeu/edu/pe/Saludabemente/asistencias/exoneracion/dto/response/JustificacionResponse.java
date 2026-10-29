package upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;

import java.time.LocalDateTime;

@Schema(description = "Justificación de ausencia")
public record JustificacionResponse(
        Long idJustificacion,
        Long idActividad,
        Long idPersona,
        String motivo,
        String urlDocumento,
        EstadoJustificacion estado,
        String observaciones,
        Long idUsuarioDictamina,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaDictamen
) {}