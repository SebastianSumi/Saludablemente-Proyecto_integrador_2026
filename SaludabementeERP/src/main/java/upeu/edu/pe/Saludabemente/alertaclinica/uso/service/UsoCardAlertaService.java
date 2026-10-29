package upeu.edu.pe.Saludabemente.alertaclinica.uso.service;

import java.util.Map;

/**
 * Preferencia de uso de los cards del menú de alertas clínicas.
 * El usuario se resuelve desde el token (JWT) en cada operación.
 */
public interface UsoCardAlertaService {

    /** Mapa ruta → conteo de clics del usuario autenticado. */
    Map<String, Integer> obtener();

    /** Incrementa en 1 el uso del card indicado para el usuario autenticado. */
    void registrar(String ruta);

    /** Borra toda la frecuencia guardada (vuelve al orden por defecto). */
    void reiniciar();
}
