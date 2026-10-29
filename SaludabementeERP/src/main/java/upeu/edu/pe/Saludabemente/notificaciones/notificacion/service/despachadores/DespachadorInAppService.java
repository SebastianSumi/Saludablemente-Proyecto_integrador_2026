package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores;

import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;

import java.util.Map;

public interface DespachadorInAppService {

    Notificacion despachar(Long idPersona,
                           String codigoEvento,
                           String origenModulo,
                           Long idOrigenReferencia,
                           Map<String, Object> variables);
}