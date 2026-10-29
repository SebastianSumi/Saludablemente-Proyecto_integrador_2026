package upeu.edu.pe.Saludabemente.personal.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload del endpoint publico {@code POST /api/v1/credenciales/verificar}.
 * Lo envia el frontend de Asistencias tras escanear un QR.
 */
public record VerificarQrRequest(
        @NotBlank(message = "El codigo QR es obligatorio")
        String codigoQrHash
) {
}