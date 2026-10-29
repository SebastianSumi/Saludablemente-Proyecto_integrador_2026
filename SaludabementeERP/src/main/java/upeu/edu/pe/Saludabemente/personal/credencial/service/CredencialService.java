package upeu.edu.pe.Saludabemente.personal.credencial.service;

import upeu.edu.pe.Saludabemente.personal.credencial.dto.response.CredencialResponse;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;

public interface CredencialService {

    /**
     * Genera la primera credencial de una persona (o una nueva si no tenía).
     * Devuelve el hash y opcionalmente el QR en Base64.
     */
    CredencialResponse generarCredencial(Long idPersona, boolean incluirQr);

    /**
     * Rota la credencial: anonimiza la anterior y crea una nueva.
     * Se usa cuando alguien perdió su QR o se sospecha filtración.
     */
    CredencialResponse rotarCredencial(Long idPersona, boolean incluirQr);

    /**
     * Anonimiza TODAS las credenciales de una persona.
     * Se invoca al inactivar una persona (soft delete).
     */
    void anonimizarCredenciales(Long idPersona);
}