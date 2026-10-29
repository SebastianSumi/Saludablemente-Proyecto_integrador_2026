package upeu.edu.pe.Saludabemente.metas.meta.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;

/**
 * STUB no-op.
 *
 * Cuando el modulo auditoria exponga AuditoriaApi.registrar(...),
 * esta implementacion se reemplaza para llamar al API real.
 * La interfaz AuditoriaMetasService NO cambia.
 */
@Slf4j
@Service
public class AuditoriaMetasServiceImpl implements AuditoriaMetasService {

    private static final String MODULO = "metas";
    private static final String ENTIDAD_META     = "META";
    private static final String ENTIDAD_CHECK_IN = "CHECK_IN";

    private static final String ACCION_CREAR               = "CREAR";
    private static final String ACCION_CAMBIAR_ESTADO      = "CAMBIAR_ESTADO";
    private static final String ACCION_CAMBIAR_ESTADO_AUTO = "CAMBIAR_ESTADO_AUTO";
    private static final String ACCION_CHECK_IN            = "CHECK_IN";
    private static final String ACCION_ACTUALIZAR_DESDE_EVALUACION = "ACTUALIZAR_DESDE_EVALUACION";

    @Override
    public void registrarMetaCreada(Long idUsuario, Long idMeta, String tipoOrigen, String pilar) {
        validar(idMeta, "idMeta");
        log.info("[STUB auditoria] modulo={} entidad={} idEntidad={} accion={} idUsuario={} " +
                        "valorNuevo={tipoOrigen={}, pilar={}}",
                MODULO, ENTIDAD_META, idMeta, ACCION_CREAR, idUsuario, tipoOrigen, pilar);
    }

    @Override
    public void registrarCambioEstado(Long idUsuario, Long idMeta,
                                      String estadoAnterior, String estadoNuevo) {
        validar(idMeta, "idMeta");
        log.info("[STUB auditoria] modulo={} entidad={} idEntidad={} accion={} idUsuario={} " +
                        "valorAnterior={estado={}} valorNuevo={estado={}}",
                MODULO, ENTIDAD_META, idMeta, ACCION_CAMBIAR_ESTADO, idUsuario,
                estadoAnterior, estadoNuevo);
    }

    @Override
    public void registrarCheckIn(Long idUsuario, Long idMeta, Long idCheckIn,
                                 String tipoCheckIn, String valorReportado) {
        validar(idMeta, "idMeta");
        validar(idCheckIn, "idCheckIn");
        log.info("[STUB auditoria] modulo={} entidad={} idEntidad={} accion={} idUsuario={} " +
                        "valorNuevo={idMeta={}, tipoCheckIn={}, valorReportado={}}",
                MODULO, ENTIDAD_CHECK_IN, idCheckIn, ACCION_CHECK_IN, idUsuario,
                idMeta, tipoCheckIn, valorReportado);
    }

    @Override
    public void registrarCambioPorScheduler(Long idMeta,
                                            String estadoAnterior, String estadoNuevo,
                                            String motivo) {
        validar(idMeta, "idMeta");
        log.info("[STUB auditoria] modulo={} entidad={} idEntidad={} accion={} idUsuario=null " +
                        "valorAnterior={estado={}} valorNuevo={estado={}, motivo={}}",
                MODULO, ENTIDAD_META, idMeta, ACCION_CAMBIAR_ESTADO_AUTO,
                estadoAnterior, estadoNuevo, motivo);
    }

    @Override
    public void registrarActualizacionDesdeEvaluacion(Long idMeta,
                                                      Long idEvaluacion,
                                                      String valorAnterior,
                                                      String valorNuevo) {
        validar(idMeta, "idMeta");
        validar(idEvaluacion, "idEvaluacion");
        log.info("[STUB auditoria] modulo={} entidad={} idEntidad={} accion={} idUsuario=null " +
                        "valorAnterior={valorActual={}} valorNuevo={valorActual={}, idEvaluacion={}}",
                MODULO, ENTIDAD_META, idMeta, ACCION_ACTUALIZAR_DESDE_EVALUACION,
                valorAnterior, valorNuevo, idEvaluacion);
    }

    private void validar(Long valor, String nombreCampo) {
        if (valor == null) {
            throw new IllegalArgumentException(nombreCampo + " no puede ser nulo");
        }
    }
}