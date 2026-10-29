package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Lote de registros offline")
public record LoteRequest(

        @NotBlank(message = "dispositivoId es obligatorio")
        @Schema(example = "device-abc-123")
        String dispositivoId,

        @NotBlank(message = "tipoFlujo es obligatorio")
        @Schema(description = "COORDINADOR | AUTOSERVICIO", example = "COORDINADOR")
        String tipoFlujo,

        @NotEmpty(message = "registros no puede estar vacío")
        @Valid
        List<RegistroLoteRequest> registros
) {}