package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores;

import java.util.Map;

public interface DespachadorEmailService {

    void despachar(String destinatario,
                   String codigoEvento,
                   Map<String, Object> variables);

    /**
     * Reenvía un correo cuyo HTML ya está renderizado. Se usa desde el
     * scheduler de reintentos: no consulta plantilla, envía el payload tal cual.
     */
    void despacharPayloadCrudo(String destinatario, String cuerpoHtml);
}