package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Registro individual dentro del lote offline")
public record RegistroLoteRequest(

        @NotBlank(message = "idLocal es obligatorio")
        @Schema(description = "UUID generado por el cliente", example = "550e8400-e29b-41d4-a716-446655440000")
        String idLocal,

        @NotNull(message = "idActividad es obligatorio")
        @Schema(example = "1")
        Long idActividad,

        @NotNull(message = "tipoAsistencia es obligatorio")
        TipoAsistencia tipoAsistencia,

        @NotNull(message = "horaCaptura es obligatoria")
        @Schema(description = "Momento en que el cliente capturó la asistencia (offline)")
        LocalDateTime horaCaptura,

        // COORDINADOR
        @Schema(description = "Hash del QR personal (solo flujo COORDINADOR)", example = "a1b2c3d4-...")
        String codigoQrPersonal,

        // AUTOSERVICIO
        @Schema(description = "Código QR general escaneado (solo flujo AUTOSERVICIO)",
                example = "UPeU-ASIS-A:1:17:1727110200000")
        String codigoQrActividad,

        @Schema(description = "Versión del QR general (solo flujo AUTOSERVICIO)", example = "17")
        Integer versionQr,

        @Schema(example = "-12.0464")
        BigDecimal latitud,

        @Schema(example = "-77.0428")
        BigDecimal longitud,

        @Schema(example = "device-abc-123")
        String dispositivoId
) {}