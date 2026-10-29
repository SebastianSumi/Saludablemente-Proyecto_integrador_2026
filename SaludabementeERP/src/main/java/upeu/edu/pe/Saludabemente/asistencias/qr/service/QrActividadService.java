package upeu.edu.pe.Saludabemente.asistencias.qr.service;

import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;

import java.time.LocalDateTime;

public interface QrActividadService {

    QrActividadResponse rotar(Long idActividad);

    QrActividadResponse obtenerActual(Long idActividad);

    /**
     * Valida el QR de actividad contra un momento de referencia
     * (now en online, horaCaptura en lote offline).
     * Acepta la versión vigente y, en gracia, la inmediata anterior.
     */
    void validarCodigoQr(String codigoQr, LocalDateTime momentoReferencia);
}