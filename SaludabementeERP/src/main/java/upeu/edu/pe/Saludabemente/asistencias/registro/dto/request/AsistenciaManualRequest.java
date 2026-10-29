package upeu.edu.pe.Saludabemente.asistencias.registro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;

@Schema(description = "Solicitud de registro manual de asistencia")
public record AsistenciaManualRequest(

        @NotNull(message = "idActividad es obligatorio")
        @Schema(description = "ID de la actividad", example = "1")
        Long idActividad,

        @NotNull(message = "idPersona es obligatorio")
        @Schema(description = "ID de la persona", example = "1")
        Long idPersona,

        @NotNull(message = "tipoAsistencia es obligatorio")
        @Schema(description = "Tipo de asistencia", example = "SIMPLE")
        TipoAsistencia tipoAsistencia,

        @Schema(description = "Latitud del escaneo", example = "-12.0464")
        BigDecimal latitud,

        @Schema(description = "Longitud del escaneo", example = "-77.0428")
        BigDecimal longitud,

        @Size(max = 300, message = "observacion no puede exceder 300 caracteres")
        @Schema(description = "Observación opcional", example = "Persona sin celular")
        String observacion
) {}