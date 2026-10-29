package upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import upeu.edu.pe.Saludabemente.alertaclinica.api.MotorAlertasApiService;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;

import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.repository.AntropometricoRepository;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalIngresoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.MetricaNutricionalDto;
import upeu.edu.pe.Saludabemente.nutricional.api.TipoMetricaNutricional;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.repository.BioquimicoRepository;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.reporte.EvaluacionAgregado;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.mapper.EvaluacionMapper;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.EvaluacionService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.recomendacionesia.api.MotorRecomendacionesApi;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final AntropometricoRepository antropometricoRepository;
    private final BioquimicoRepository bioquimicoRepository;
    private final EvaluacionMapper evaluacionMapper;
    private final PersonaService personaService;
    private final PeriodoService periodoService;
    private final MotorAlertasApiService motorAlertas;
    private final MotorRecomendacionesApi motorRecomendaciones;

    // ===================================================================
    // Consultas
    // ===================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<EvaluacionResumen> buscar(EstadoEvaluacion estado,
                                          String periodo,
                                          Long idPersona,
                                          Pageable pageable) {
        return evaluacionRepository.buscar(estado, periodo, idPersona, pageable)
                .map(evaluacionMapper::toResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionAgregado agregados(String periodo) {
        List<Object[]> rows = evaluacionRepository.agregados(periodo, EstadoEvaluacion.ANULADA);

        if (rows.isEmpty() || rows.get(0) == null) {
            return new EvaluacionAgregado(0L, null, null, null);
        }

        Object[] row = rows.get(0);
        long total = row[0] == null ? 0L : ((Number) row[0]).longValue();
        Double avgImc = row[1] == null ? null : ((Number) row[1]).doubleValue();
        Double avgGrasa = row[2] == null ? null : ((Number) row[2]).doubleValue();
        Double avgMasa = row[3] == null ? null : ((Number) row[3]).doubleValue();

        return new EvaluacionAgregado(total, avgImc, avgGrasa, avgMasa);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionResponse obtener(Long id) {
        return evaluacionMapper.toResponse(obtenerOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionResumen> historialPorPersona(Long idPersona) {
        return evaluacionRepository.findByIdPersonaOrderByFechaEvaluacionDesc(idPersona)
                .stream()
                .map(evaluacionMapper::toResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionResumen> incompletasPorPeriodo(String periodo) {
        return evaluacionRepository.findIncompletasByPeriodo(periodo, EstadoEvaluacion.PENDIENTE)
                .stream()
                .map(evaluacionMapper::toResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoResponse> listarPeriodosDisponibles() {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "codigo");

        List<PeriodoResponse> vigentes = periodoService.buscar(EstadoPeriodo.VIGENTE, sort);
        List<PeriodoResponse> planificados = periodoService.buscar(EstadoPeriodo.PLANIFICADO, sort);

        List<PeriodoResponse> disponibles = new ArrayList<>(vigentes.size() + planificados.size());
        disponibles.addAll(vigentes);
        disponibles.addAll(planificados);
        return disponibles;
    }

    // ===================================================================
    // Comandos
    // ===================================================================

    @Override
    @Transactional
    public EvaluacionResponse iniciar(EvaluacionInitRequest request) {
        // 1. Validar que el periodo exista y no esté CERRADO.
        PeriodoResponse periodo = periodoService.obtener(request.getIdPeriodoId());
        if (periodo.getEstado() == EstadoPeriodo.CERRADO) {
            throw new BusinessRuleException(
                    "No se puede iniciar evaluación en un periodo CERRADO: "
                            + periodo.getCodigo());
        }

        // 2. Capa anticorrupción: snapshot clínico desde Personal.
        PersonaSnapshotClinicoDTO snapshot = personaService.obtenerSnapshotClinico(request.getIdPersona());

        // 3. RN-01: la persona debe estar activa.
        if (!snapshot.isActivo()) {
            throw new BusinessRuleException(
                    "No se puede iniciar evaluación: la persona está inactiva");
        }

        // 4. RN-02: unicidad semestral contra el código del periodo.
        validarUnicidadSemestral(request.getIdPersona(), periodo.getCodigo());

        // 5. Congelar el snapshot + el código del periodo en la cabecera.
        EvaluacionNutricional entity = evaluacionMapper.toEntity(request);
        entity.setIdPeriodo(periodo.getCodigo());
        entity.setDocumentoIdentidad(snapshot.documentoIdentidad());
        entity.setNombresCompletos(snapshot.nombresCompletos());
        entity.setEdadAlMomento(snapshot.edadAlMomento());
        entity.setSexoAlMomento(snapshot.sexo());
        entity.setAreaTrabajoAlMomento(snapshot.areaTrabajo());
        entity.setEstadoEvaluacion(EstadoEvaluacion.PENDIENTE);

        EvaluacionNutricional guardada = evaluacionRepository.save(entity);
        return evaluacionMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public EvaluacionResponse actualizarObservaciones(Long id, String observaciones) {
        EvaluacionNutricional entity = obtenerOFallar(id);

        if (entity.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede modificar una evaluación anulada");
        }

        entity.setObservaciones(observaciones);
        return evaluacionMapper.toResponse(evaluacionRepository.save(entity));
    }

    @Override
    @Transactional
    public void anular(Long id) {
        EvaluacionNutricional entity = obtenerOFallar(id);

        if (entity.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException("La evaluación ya está anulada");
        }

        int filas = evaluacionRepository.softDelete(id, EstadoEvaluacion.ANULADA);
        if (filas == 0) {
            throw new ResourceNotFoundException("Evaluación no encontrada: " + id);
        }
    }

    // ===================================================================
    // API interna para subdominios del propio módulo
    // ===================================================================

    @Override
    @Transactional(readOnly = true)
    public EvaluacionNutricional obtenerOFallar(Long id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación nutricional no encontrada: " + id));
    }

    @Override
    @Transactional
    public void evaluarCompletitudYDisparar(Long idEvaluacion) {
        EvaluacionNutricional entity = obtenerOFallar(idEvaluacion);

        if (entity.getEstadoEvaluacion() != EstadoEvaluacion.PENDIENTE) {
            return;
        }

        boolean tieneAntropometrico =
                antropometricoRepository.existsByEvaluacion_IdEvaluacion(idEvaluacion);
        boolean tieneBioquimico =
                bioquimicoRepository.existsByEvaluacion_IdEvaluacion(idEvaluacion);

        if (!tieneAntropometrico || !tieneBioquimico) {
            return;
        }

        entity.setEstadoEvaluacion(EstadoEvaluacion.COMPLETA);
        evaluacionRepository.save(entity);

        // Alertas: dentro de la transacción.
        dispararMotorAlertas(entity);

        // Recomendaciones IA: post-commit.
        Long idEvalRec = entity.getIdEvaluacion();
        Long idPersRec = entity.getIdPersona();
        registrarPostCommit(() -> dispararRecomendacionesIA(idEvalRec, idPersRec));
    }

    /**
     * Sincroniza las metas del colaborador con los indicadores medidos
     * en la evaluación recién completada.
     *
     * <p>
     * Se invoca post-commit: si Metas falla, la evaluación ya está
     * persistida. El error se loguea y se continúa.
     * </p>
     */
    private void sincronizarMetas(Long idPersona,
                                  Long idEvaluacion,
                                  Map<String, BigDecimal> indicadores) {
        if (indicadores.isEmpty()) {
            return;
        }

        log.debug("[NUTRICIONAL→METAS] Sincronización pendiente para evaluación {}: {} indicadores",
                idEvaluacion, indicadores.size());

        // TODO: descomentar cuando Metas publique MetaActualizacionApi
        // try {
        // ResultadoActualizacionDTO resultado =
        // metaActualizacionApi.actualizarDesdeEvaluacion(
        // idPersona, idEvaluacion, indicadores);
        //
        // log.info("Sincronización con metas para evaluación {}: {} evaluadas, "
        // + "{} actualizadas, {} completadas",
        // idEvaluacion,
        // resultado.metasEvaluadas(),
        // resultado.metasActualizadas(),
        // resultado.metasCompletadas());
        // } catch (Exception e) {
        // log.warn("Fallo al sincronizar metas para evaluación {}: {}",
        // idEvaluacion, e.getMessage());
        // }
    }

    /**
     * Construye el mapa de indicadores con valor real.
     * Omite los que vienen null.
     * Vocabulario cerrado — ver contrato con Metas §4.2.
     */
    private Map<String, BigDecimal> ensamblarIndicadoresParaMetas(EvaluacionNutricional entity) {
        Map<String, BigDecimal> indicadores = new LinkedHashMap<>();

        var antro = entity.getDetalleAntropometrico();
        if (antro != null) {
            putSiNoEsNulo(indicadores, "peso", antro.getPesoKg());
            putSiNoEsNulo(indicadores, "estatura", antro.getEstaturaCm());
            putSiNoEsNulo(indicadores, "imc", antro.getImc());
            putSiNoEsNulo(indicadores, "perimetroAbdominal",
                    antro.getPerimetroAbdominalCm());
            putSiNoEsNulo(indicadores, "porcentajeGrasa", antro.getPorcentajeGrasa());
            putSiNoEsNulo(indicadores, "porcentajeMasaMuscular",
                    antro.getPorcentajeMasaMuscular());
            putSiNoEsNulo(indicadores, "porcentajeGrasaVisceral",
                    antro.getPorcentajeGrasaVisceral());
        }

        var bio = entity.getDetalleBioquimico();
        if (bio != null) {
            putSiNoEsNulo(indicadores, "glucosa", bio.getGlucosa());
            putSiNoEsNulo(indicadores, "colesterol", bio.getColesterol());
            putSiNoEsNulo(indicadores, "trigliceridos", bio.getTrigliceridos());

            if (bio.getPresionSistolica() != null) {
                indicadores.put("presionSistolica",
                        BigDecimal.valueOf(bio.getPresionSistolica()));
            }
            if (bio.getPresionDiastolica() != null) {
                indicadores.put("presionDiastolica",
                        BigDecimal.valueOf(bio.getPresionDiastolica()));
            }
        }

        return indicadores;
    }

    private void putSiNoEsNulo(Map<String, BigDecimal> map, String clave, BigDecimal valor) {
        if (valor != null) {
            map.put(clave, valor);
        }
    }

    // ===================================================================
    // Integración con Alertas Clínicas (Patrón B)
    // ===================================================================

    /**
     * Ensambla el payload y lo envía al motor de Alertas.
     *
     * <p>
     * <b>Rollback débil (Opción B del contrato):</b> si el motor falla,
     * se loguea y se continúa. La evaluación ya está persistida. El motor
     * corre en {@code REQUIRES_NEW}, así que un fallo suyo tampoco afecta
     * la transacción actual.
     * </p>
     */
    private void dispararMotorAlertas(EvaluacionNutricional entity) {
        List<MetricaNutricionalDto> metricas = ensamblarMetricas(entity);

        if (metricas.isEmpty()) {
            return;
        }

        EvaluacionNutricionalIngresoDto dto = new EvaluacionNutricionalIngresoDto(
                entity.getIdEvaluacion(),
                entity.getIdPersona(),
                entity.getFechaEvaluacion(),
                metricas);

        try {
            motorAlertas.procesarNutricional(dto);
        } catch (Exception e) {
            log.error("Motor de alertas falló para evaluación {}: {}",
                    entity.getIdEvaluacion(), e.getMessage(), e);
        }
    }

    /**
     * Construye la lista de métricas con valor real.
     * Omite las que vienen null — el motor no debe validar nulos.
     * Presión arterial como dos métricas separadas.
     */
    private List<MetricaNutricionalDto> ensamblarMetricas(EvaluacionNutricional entity) {
        List<MetricaNutricionalDto> metricas = new ArrayList<>();

        var antro = entity.getDetalleAntropometrico();
        if (antro != null) {
            addSiNoEsNulo(metricas, TipoMetricaNutricional.IMC, antro.getImc());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.PERIMETRO_ABDOMINAL,
                    antro.getPerimetroAbdominalCm());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.GRASA_CORPORAL,
                    antro.getPorcentajeGrasa());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.MASA_MUSCULAR,
                    antro.getPorcentajeMasaMuscular());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.GRASA_VISCERAL,
                    antro.getPorcentajeGrasaVisceral());
        }

        var bio = entity.getDetalleBioquimico();
        if (bio != null) {
            addSiNoEsNulo(metricas, TipoMetricaNutricional.GLUCOSA, bio.getGlucosa());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.COLESTEROL, bio.getColesterol());
            addSiNoEsNulo(metricas, TipoMetricaNutricional.TRIGLICERIDOS,
                    bio.getTrigliceridos());

            if (bio.getPresionSistolica() != null) {
                metricas.add(new MetricaNutricionalDto(
                        TipoMetricaNutricional.PRESION_SISTOLICA,
                        BigDecimal.valueOf(bio.getPresionSistolica())));
            }
            if (bio.getPresionDiastolica() != null) {
                metricas.add(new MetricaNutricionalDto(
                        TipoMetricaNutricional.PRESION_DIASTOLICA,
                        BigDecimal.valueOf(bio.getPresionDiastolica())));
            }
        }

        return metricas;
    }

    private void addSiNoEsNulo(List<MetricaNutricionalDto> lista,
                               TipoMetricaNutricional tipo,
                               BigDecimal valor) {
        if (valor != null) {
            lista.add(new MetricaNutricionalDto(tipo, valor));
        }
    }

    // ===================================================================
    // Helpers privados
    // ===================================================================

    /**
     * RN-02: no puede existir evaluación PENDIENTE ni COMPLETA para la
     * misma persona en el mismo periodo. Las ANULADAS no cuentan.
     */
    private void validarUnicidadSemestral(Long idPersona, String idPeriodo) {
        boolean existe = evaluacionRepository.existsActivaPorPersonaYPeriodo(
                idPersona,
                idPeriodo,
                List.of(EstadoEvaluacion.PENDIENTE, EstadoEvaluacion.COMPLETA));

        if (existe) {
            throw new BusinessRuleException(
                    "La persona " + idPersona
                            + " ya tiene una evaluación activa en el periodo " + idPeriodo);
        }
    }

    // ===================================================================
// Integración con Recomendaciones IA
// ===================================================================

    /**
     * Registra un efecto para ejecutarse después del commit de la transacción.
     * Si no hay transacción activa, ejecuta el efecto inmediatamente.
     */
    private void registrarPostCommit(Runnable efecto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            efecto.run();
                        }
                    });
        } else {
            efecto.run();
        }
    }

    /**
     * Dispara la generación de recomendaciones IA post-commit.
     *
     * <p>El contrato dice que la generación corre en background con outbox
     * en el lado de Recomendaciones IA. Las validaciones internas
     * (evaluación completa, aptitud física presente, sin recomendación vigente)
     * las hace el motor. Si algo falla, se loguea y se continúa — el reintento
     * lo asume Recomendaciones IA.</p>
     */
    private void dispararRecomendacionesIA(Long idEvaluacion, Long idPersona) {
        log.debug("[NUTRICIONAL→RECOMENDACIONES] Disparando generación para evaluación {}",
                idEvaluacion);

        try {
            motorRecomendaciones.dispararGeneracion(idEvaluacion, idPersona);
        } catch (Exception e) {
            log.warn("Fallo al disparar recomendaciones IA para evaluación {}: {}",
                    idEvaluacion, e.getMessage());
        }
    }
}