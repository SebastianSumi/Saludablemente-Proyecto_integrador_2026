package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Insignia motivacional")
public record InsigniaResponse(
        @Schema(example = "CONSTANTE") String codigo,
        @Schema(example = "🎯") String emoji,
        @Schema(example = "Constante") String nombre,
        @Schema(example = "≥ 90% sin rachas de faltas") String descripcion
) {}