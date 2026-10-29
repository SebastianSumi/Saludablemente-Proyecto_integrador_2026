package upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de justificación de ausencia")
public record JustificacionRequest(

        @NotNull(message = "idActividad es obligatorio")
        @Schema(example = "1")
        Long idActividad,

        @NotNull(message = "idPersona es obligatorio")
        @Schema(example = "5")
        Long idPersona,

        @NotBlank(message = "motivo es obligatorio")
        @Size(max = 300, message = "motivo no puede exceder 300 caracteres")
        @Schema(example = "Descanso médico por gripe")
        String motivo,

        @Size(max = 250, message = "urlDocumento no puede exceder 250 caracteres")
        @Schema(example = "https://storage.upeu.edu.pe/justificaciones/abc.pdf")
        String urlDocumento
) {}