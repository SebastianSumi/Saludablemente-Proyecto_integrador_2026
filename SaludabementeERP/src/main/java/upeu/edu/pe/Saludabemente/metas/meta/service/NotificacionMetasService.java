package upeu.edu.pe.Saludabemente.metas.meta.service;

/**
 * Fachada local del modulo metas hacia notificaciones.
 *
 * NO es una API publica de notificaciones. Es un traductor local que
 * convierte eventos de dominio de metas en llamadas al NotificacionApi
 * generico cuando ese modulo exista.
 *
 * Por ahora se implementa como stub no-op (log a consola).
 */
public interface NotificacionMetasService {

    void notificarMetaDesviada(Long idPersona, Long idMeta, String descripcionMeta);

    void notificarMetaCompletada(Long idPersona, Long idMeta, String descripcionMeta);

    void notificarEvaluador(Long idEvaluador, Long idMeta, String motivo);
}