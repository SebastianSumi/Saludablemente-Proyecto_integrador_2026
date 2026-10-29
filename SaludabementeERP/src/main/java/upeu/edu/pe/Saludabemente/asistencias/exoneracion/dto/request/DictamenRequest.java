package upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dictamen sobre una justificación")
public record DictamenRequest(

        @NotBlank(message = "estado es obligatorio")
        @Schema(description = "APROBADO o RECHAZADO", example = "APROBADO")
        String estado,

        @Size(max = 300, message = "observaciones no puede exceder 300 caracteres")
        @Schema(example = "Documento válido, se exonera la falta")
        String observaciones,

        @NotNull(message = "idUsuarioDictamina es obligatorio")
        @Schema(example = "1")
        Long idUsuarioDictamina
) {}