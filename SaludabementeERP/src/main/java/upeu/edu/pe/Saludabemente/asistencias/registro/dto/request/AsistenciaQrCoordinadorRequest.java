package upeu.edu.pe.Saludabemente.asistencias.registro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;

@Schema(description = "Solicitud de registro por QR personal escaneado por el coordinador")
public record AsistenciaQrCoordinadorRequest(

        @NotNull(message = "idActividad es obligatorio")
        @Schema(description = "ID de la actividad", example = "1")
        Long idActividad,

        @NotBlank(message = "codigoQrPersonal es obligatorio")
        @Schema(description = "Hash del QR personal escaneado", example = "a1b2c3d4-...")
        String codigoQrPersonal,

        @NotBlank(message = "codigoQrActividad es obligatorio")
        @Schema(description = "Código QR de actividad vigente (el proyectado)", example = "UPeU-ASIS-A:1:17:1727110200000")
        String codigoQrActividad,

        @NotNull(message = "tipoAsistencia es obligatorio")
        @Schema(description = "Tipo de asistencia", example = "SIMPLE")
        TipoAsistencia tipoAsistencia,

        @Schema(description = "Latitud del coordinador", example = "-12.0464")
        BigDecimal latitud,

        @Schema(description = "Longitud del coordinador", example = "-77.0428")
        BigDecimal longitud,

        @Schema(description = "ID del dispositivo del coordinador", example = "device-abc-123")
        String dispositivoId,

        @Schema(description = "UUID local para idempotencia", example = "550e8400-e29b-41d4-a716-446655440000")
        String idLocal
) {}