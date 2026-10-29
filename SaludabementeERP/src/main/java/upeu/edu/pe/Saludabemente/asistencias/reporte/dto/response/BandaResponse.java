package upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Banda motivacional por porcentaje")
public record BandaResponse(
        @Schema(example = "BUENO") String codigo,
        @Schema(example = "Buen ritmo") String etiqueta,
        @Schema(example = "#22C55E") String color,
        @Schema(example = "🟢") String emoji
) {}