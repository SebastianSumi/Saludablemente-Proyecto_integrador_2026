package upeu.edu.pe.Saludabemente.metas.meta.service;

/**
 * Fachada local del modulo metas hacia auditoria.
 *
 * NO es una API publica de auditoria. Traduce eventos de dominio de
 * metas al AuditoriaApi generico cuando ese modulo exista.
 *
 * Por ahora se implementa como stub no-op (log a consola).
 */
public interface AuditoriaMetasService {

    void registrarMetaCreada(Long idUsuario, Long idMeta, String tipoOrigen, String pilar);

    void registrarCambioEstado(Long idUsuario, Long idMeta,
                               String estadoAnterior, String estadoNuevo);

    void registrarCheckIn(Long idUsuario, Long idMeta, Long idCheckIn,
                          String tipoCheckIn, String valorReportado);

    void registrarCambioPorScheduler(Long idMeta,
                                     String estadoAnterior, String estadoNuevo,
                                     String motivo);

    void registrarActualizacionDesdeEvaluacion(Long idMeta,
                                               Long idEvaluacion,
                                               String valorAnterior,
                                               String valorNuevo);
}