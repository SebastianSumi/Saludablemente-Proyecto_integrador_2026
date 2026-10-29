package upeu.edu.pe.Saludabemente.personal.credencial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.EstadoCredencial;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredencialResponse {

    private Long id;
    private Long personaId;
    private String codigoQrHash;
    private LocalDateTime fechaEmision;
    private EstadoCredencial estado;

    /**
     * Representación PNG del QR en Base64.
     * Solo se llena cuando el cliente lo pide explícitamente (query param incluirQr=true).
     * Es null en respuestas estándar.
     */
    private String qrBase64;
}