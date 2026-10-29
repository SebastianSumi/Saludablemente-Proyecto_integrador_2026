package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado agregado del lote")
public record LoteResponse(
        @Schema(example = "50")
        Integer procesados,

        @Schema(example = "45")
        Integer synced,

        @Schema(example = "3")
        Integer cuarentena,

        @Schema(example = "2")
        Integer failed,

        List<ResultadoRegistroResponse> resultados
) {}