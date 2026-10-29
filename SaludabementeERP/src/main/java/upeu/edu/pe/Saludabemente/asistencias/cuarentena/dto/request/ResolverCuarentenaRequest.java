package upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Decisión sobre un registro en cuarentena")
public record ResolverCuarentenaRequest(

        @NotBlank(message = "decision es obligatorio")
        @Schema(description = "APROBAR o RECHAZAR", example = "APROBAR")
        String decision,

        @Size(max = 300, message = "observaciones no puede exceder 300 caracteres")
        @Schema(description = "Observaciones del administrador", example = "Coordinador confirma presencia")
        String observaciones,

        @NotNull(message = "idUsuarioResuelve es obligatorio")
        @Schema(description = "ID del usuario que resuelve", example = "1")
        Long idUsuarioResuelve
) {}