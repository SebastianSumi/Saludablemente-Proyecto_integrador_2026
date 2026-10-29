package upeu.edu.pe.Saludabemente.personal.api;

/**
 * Resultado de verificar un QR para Asistencia.
 * El módulo Asistencias lo consume para saber si aceptar el marcado.
 */
public record VerificacionQrResponse(
        boolean valido,
        Long idPersona,
        String mensaje
) {
    public static VerificacionQrResponse valido(Long idPersona) {
        return new VerificacionQrResponse(true, idPersona, "QR válido");
    }

    public static VerificacionQrResponse invalido(String motivo) {
        return new VerificacionQrResponse(false, null, motivo);
    }
}