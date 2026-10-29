package upeu.edu.pe.Saludabemente.asistencias.qr.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "QR general vigente de una actividad")
public record QrActividadResponse(
        @Schema(description = "ID del registro QR", example = "42")
        Long idQrActividad,

        @Schema(description = "ID de la actividad", example = "1")
        Long idActividad,

        @Schema(description = "Versión del QR", example = "17")
        Integer version,

        @Schema(description = "Código QR completo escaneable", example = "UPeU-ASIS-A:1:17:1727110200000")
        String codigoQr,

        @Schema(description = "Inicio de la ventana de validez")
        LocalDateTime ventanaInicio,

        @Schema(description = "Fin de la ventana de validez")
        LocalDateTime ventanaFin,

        @Schema(description = "Segundos hasta que expire este QR", example = "180")
        Integer expiraEnSegundos,

        @Schema(description = "Si está activo")
        Boolean activo
) {}