package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado del procesamiento de un registro del lote")
public record ResultadoRegistroResponse(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        String idLocal,

        @Schema(description = "SYNCED | CUARENTENA | FAILED", example = "SYNCED")
        String resultado,

        @Schema(description = "Motivo del resultado", example = "OK")
        String motivo,

        @Schema(description = "ID generado en ASISTENCIA, si aplica", example = "123")
        Long idAsistencia,

        @Schema(description = "ID generado en ASISTENCIA_CUARENTENA, si aplica", example = "45")
        Long idCuarentena
) {}