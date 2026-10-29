package upeu.edu.pe.Saludabemente.personal.credencial.dto.response;

import upeu.edu.pe.Saludabemente.personal.credencial.entity.EstadoCredencial;

import java.time.LocalDateTime;

public record CredencialResponse(
        Long id,
        Long personaId,
        String codigoQrHash,
        LocalDateTime fechaEmision,
        EstadoCredencial estado,
        String qrBase64
) {}
