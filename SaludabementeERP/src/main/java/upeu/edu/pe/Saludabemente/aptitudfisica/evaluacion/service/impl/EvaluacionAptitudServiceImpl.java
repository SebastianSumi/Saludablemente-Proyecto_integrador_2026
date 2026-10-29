package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.api.MotorAlertasApiService;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.EvaluacionFisicaIngresoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.MetricaFisicaDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.TipoMetrica;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.ObservacionesRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.response.EvaluacionAptitudResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.AptitudAgregadoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.DictamenGlobal;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.mapper.EvaluacionAptitudMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.repository.EvaluacionAptitudRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.MetricasAptitudUtil;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionAptitudServiceImpl implements EvaluacionAptitudService {

    private static final String NIVEL_BAJO = "BAJO";

    private final EvaluacionAptitudRepository repository;
    private final EvaluacionAptitudMapper mapper;
    private final PersonaService personaService;
    private final PeriodoService periodoService;
    private final MetricasAptitudUtil metricas;
    private final MotorAlertasApiService motorAlertas;

    // -------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<EvaluacionResumen> buscar(Long idPeriodo, EstadoEvaluacion estado,
                                          String area, String texto, Pageable pageable) {
        return repository.buscar(idPeriodo, estado, area, texto, pageable)
                .map(mapper::toResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionAptitudResponse obtener(Long id) {
        return mapper.toResponse(buscarConDetalleOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionResumen> historial(Long idPersona) {
        return repository.findByIdPersonaOrderByFechaEvaluacionDesc(idPersona)
                .stream()
                .map(mapper::toResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AptitudAgregadoDto obtenerAgregados(Long idPeriodo) {
        return repository.obtenerAgregados(idPeriodo);
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public EvaluacionAptitudResponse iniciar(EvaluacionInitRequest request) {
        // 1. Validar período vigente
        PeriodoResponse vigente = periodoService.obtenerVigente();
        if (!vigente.getId().equals(request.idPeriodo())) {
            throw new BusinessRuleException(
                    "El período " + request.idPeriodo() + " no es el vigente ("
                            + vigente.getId() + " - " + vigente.getCodigo() + ")");
        }

        // 2. Snapshot de Personal
        PersonaSnapshotClinicoDTO snapshot = personaService.obtenerSnapshotClinico(request.idPersona());
        if (!snapshot.isActivo()) {
            throw new BusinessRuleException(
                    "No se puede iniciar evaluación a una persona inactiva: " + request.idPersona());
        }

        // 3. Unicidad semestral
        if (repository.existsByIdPersonaAndIdPeriodo(request.idPersona(), request.idPeriodo())) {
            throw new BusinessRuleException(
                    "Ya existe evaluación de aptitud para la persona "
                            + request.idPersona() + " en el período " + request.idPeriodo());
        }

        // 4. Persistir cabecera con snapshot congelado
        EvaluacionAptitud entity = mapper.toEntity(snapshot, request.idPeriodo(), request.fechaEvaluacion());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void anular(Long id) {
        EvaluacionAptitud entity = buscarOFallar(id);
        if (entity.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException("La evaluación ya está ANULADA: " + id);
        }
        entity.setEstadoEvaluacion(EstadoEvaluacion.ANULADA);
        repository.save(entity);
    }

    @Override
    @Transactional
    public EvaluacionAptitudResponse actualizarObservaciones(Long id, ObservacionesRequest request) {
        EvaluacionAptitud entity = buscarOFallar(id);
        if (entity.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede actualizar una evaluación ANULADA: " + id);
        }
        entity.setObservacionesRestricciones(request.observaciones());
        return mapper.toResponse(repository.save(entity));
    }

    // -------------------------------------------------------------------
    // Completitud — disparada por FuerzaServiceImpl y CardioServiceImpl
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public void evaluarCompletitud(Long idEvaluacion) {
        EvaluacionAptitud entity = buscarConDetalleOFallar(idEvaluacion);

        // Sin ambos detalles, no hay nada que consolidar.
        if (entity.getDetalleFuerza() == null || entity.getDetalleCardio() == null) {
            return;
        }
        // Si la cabecera está ANULADA, no se re-consolida.
        if (entity.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            return;
        }

        String dxFuerza = entity.getDetalleFuerza().getDxFuerzaGlobal();
        String dxCardio = entity.getDetalleCardio().getDxCardiovascular();

        entity.setDictamenGlobal(calcularDictamen(dxFuerza, dxCardio));
        entity.setFitnessScore(metricas.calcularFitnessScore(
                entity.getDetalleFuerza().getDxCore(),
                entity.getDetalleFuerza().getDxTrenSuperior(),
                entity.getDetalleFuerza().getDxTrenInferior(),
                dxCardio));
        entity.setEstadoEvaluacion(EstadoEvaluacion.COMPLETA);
        repository.save(entity);

        // Notificar al motor de alertas — dentro de try-catch local.
        dispararMotorAlertas(entity);
    }

    // -------------------------------------------------------------------
    // Uso interno
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public EvaluacionAptitud buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación de aptitud no encontrada: " + id));
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private EvaluacionAptitud buscarConDetalleOFallar(Long id) {
        return repository.findByIdConDetalle(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación de aptitud no encontrada: " + id));
    }

    private DictamenGlobal calcularDictamen(String dxFuerza, String dxCardio) {
        boolean fuerzaBaja = NIVEL_BAJO.equals(dxFuerza);
        boolean cardioBajo = NIVEL_BAJO.equals(dxCardio);
        if (fuerzaBaja && cardioBajo) return DictamenGlobal.NO_APTO;
        if (fuerzaBaja || cardioBajo) return DictamenGlobal.APTO_CON_RESTRICCIONES;
        return DictamenGlobal.APTO;
    }

    /**
     * Ensambla el payload y notifica al motor de alertas.
     * <p>
     * El try-catch es local y no propaga la excepción: un fallo del motor NO
     * revierte la evaluación física ya persistida. El motor abre su propia
     * transacción ({@code REQUIRES_NEW}), así que la defensa es doble.
     */
    private void dispararMotorAlertas(EvaluacionAptitud entity) {
        try {
            List<MetricaFisicaDto> metricasDto = new ArrayList<>();

            if (entity.getDetalleFuerza() != null) {
                var f = entity.getDetalleFuerza();
                metricasDto.add(new MetricaFisicaDto(TipoMetrica.ABDOMINALES,
                        BigDecimal.valueOf(f.getRepeticionesAbdominales())));
                metricasDto.add(new MetricaFisicaDto(TipoMetrica.PLANCHAS,
                        BigDecimal.valueOf(f.getRepeticionesFlexionBrazo())));
                metricasDto.add(new MetricaFisicaDto(TipoMetrica.SALTO_SIN_IMPULSO,
                        f.getSaltoLongitudCm()));
            }

            if (entity.getDetalleCardio() != null) {
                var c = entity.getDetalleCardio();
                metricasDto.add(new MetricaFisicaDto(TipoMetrica.SALTA_SOGA,
                        BigDecimal.valueOf(c.getRepeticionesSoga30s())));
                metricasDto.add(new MetricaFisicaDto(TipoMetrica.CARRERA_400M,
                        BigDecimal.valueOf(c.getTiempoCarrera400mSeg())));
            }

            EvaluacionFisicaIngresoDto dto = new EvaluacionFisicaIngresoDto(
                    entity.getId(),
                    entity.getIdPersona(),
                    entity.getFechaEvaluacion(),
                    metricasDto
            );

            motorAlertas.procesarFisica(dto);

        } catch (Exception e) {
            log.error("Error al procesar evaluacion fisica {} en el motor de alertas: {}",
                    entity.getId(), e.getMessage(), e);
            // No se propaga: un fallo del motor no revierte la evaluación.
        }
    }
}