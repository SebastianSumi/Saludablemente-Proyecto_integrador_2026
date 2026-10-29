package upeu.edu.pe.Saludabemente.personal.api;

/**
 * API publica del modulo Personal para verificacion de credenciales QR.
 * Consumido por el modulo Asistencias.
 */
public interface CredencialApiService {

    /**
     * Verifica un hash de QR contra la base de credenciales.
     * Devuelve {@code valido = true} solo si existe una credencial con ese
     * hash y estado {@code ACTIVA}. En cualquier otro caso devuelve
     * {@code valido = false} con un mensaje explicativo.
     *
     * <p>Nunca lanza excepcion. Siempre devuelve respuesta tipada.</p>
     */
    VerificacionQrResponse verificarQrParaAsistencia(String codigoQrHash);
}