package upeu.edu.pe.Saludabemente.metas.meta.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.metas.meta.service.NotificacionMetasService;

/**
 * STUB no-op.
 *
 * Cuando el modulo notificaciones exponga NotificacionApi.enviar(...),
 * esta implementacion se reemplaza para llamar al API real.
 * La interfaz NotificacionMetasService NO cambia.
 */
@Slf4j
@Service
public class NotificacionMetasServiceImpl implements NotificacionMetasService {

    private static final String ORIGEN = "metas";
    private static final String EVENTO_META_DESVIADA     = "META_DESVIADA";
    private static final String EVENTO_META_COMPLETADA   = "META_COMPLETADA";
    private static final String EVENTO_META_CRITICA_EVAL = "META_CRITICA_EVALUADOR";

    @Override
    public void notificarMetaDesviada(Long idPersona, Long idMeta, String descripcionMeta) {
        validar(idPersona, idMeta, descripcionMeta, "descripcionMeta");
        log.info("[STUB notificacion] origen={} evento={} idPersona={} idMeta={} vars={descripcionMeta={}}",
                ORIGEN, EVENTO_META_DESVIADA, idPersona, idMeta, descripcionMeta);
    }

    @Override
    public void notificarMetaCompletada(Long idPersona, Long idMeta, String descripcionMeta) {
        validar(idPersona, idMeta, descripcionMeta, "descripcionMeta");
        log.info("[STUB notificacion] origen={} evento={} idPersona={} idMeta={} vars={descripcionMeta={}}",
                ORIGEN, EVENTO_META_COMPLETADA, idPersona, idMeta, descripcionMeta);
    }

    @Override
    public void notificarEvaluador(Long idEvaluador, Long idMeta, String motivo) {
        validar(idEvaluador, idMeta, motivo, "motivo");
        log.info("[STUB notificacion] origen={} evento={} idEvaluador={} idMeta={} vars={motivo={}}",
                ORIGEN, EVENTO_META_CRITICA_EVAL, idEvaluador, idMeta, motivo);
    }

    private void validar(Long idDestinatario, Long idMeta, String texto, String nombreCampo) {
        if (idDestinatario == null) {
            throw new IllegalArgumentException("idDestinatario no puede ser nulo");
        }
        if (idMeta == null) {
            throw new IllegalArgumentException("idMeta no puede ser nulo");
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " no puede estar vacio");
        }
    }
}