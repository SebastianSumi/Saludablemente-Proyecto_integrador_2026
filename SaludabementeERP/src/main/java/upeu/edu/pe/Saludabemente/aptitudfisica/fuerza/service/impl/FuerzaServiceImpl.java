package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.request.FuerzaRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity.DetalleFuerzaResistencia;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.mapper.FuerzaMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.repository.FuerzaRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.service.FuerzaService;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.MetricasAptitudUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class FuerzaServiceImpl implements FuerzaService {

    private final FuerzaRepository fuerzaRepository;
    private final FuerzaMapper fuerzaMapper;
    private final EvaluacionAptitudService evaluacionService;
    private final MetricasAptitudUtil metricas;

    @Override
    @Transactional
    public FuerzaResponse registrar(Long idEvaluacion, FuerzaRequest request) {
        EvaluacionAptitud cabecera = evaluacionService.buscarOFallar(idEvaluacion);
        validarEstadoEditable(cabecera);
        validarNoExiste(idEvaluacion);

        DetalleFuerzaResistencia detalle = DetalleFuerzaResistencia.builder()
                .evaluacion(cabecera)
                .build();
        aplicarMetricas(detalle, request, cabecera);

        DetalleFuerzaResistencia guardado = fuerzaRepository.save(detalle);
        cabecera.setDetalleFuerza(guardado);

        // Disparar completitud (interna, no requiere nuevo commit)
        evaluacionService.evaluarCompletitud(idEvaluacion);

        return fuerzaMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public FuerzaResponse actualizar(Long idEvaluacion, FuerzaRequest request) {
        EvaluacionAptitud cabecera = evaluacionService.buscarOFallar(idEvaluacion);
        validarEstadoNoAnulada(cabecera);

        DetalleFuerzaResistencia detalle = fuerzaRepository.findByEvaluacionId(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Detalle de fuerza no encontrado para evaluación: " + idEvaluacion));

        aplicarMetricas(detalle, request, cabecera);

        DetalleFuerzaResistencia guardado = fuerzaRepository.save(detalle);
        cabecera.setDetalleFuerza(guardado);

        // Recalcular dictamen y fitness score con los nuevos valores
        evaluacionService.evaluarCompletitud(idEvaluacion);

        return fuerzaMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public FuerzaResponse consultar(Long idEvaluacion) {
        return fuerzaRepository.findByEvaluacionId(idEvaluacion)
                .map(fuerzaMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Detalle de fuerza no encontrado para evaluación: " + idEvaluacion));
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private void aplicarMetricas(DetalleFuerzaResistencia detalle, FuerzaRequest request,
                                 EvaluacionAptitud cabecera) {
        String sexo  = cabecera.getSexoAlMomento();
        Integer edad = cabecera.getEdadAlMomento();

        String dxCore   = metricas.dxCore(request.repeticionesAbdominales(), sexo, edad);
        String dxSup    = metricas.dxTrenSuperior(request.repeticionesFlexionBrazo(), sexo, edad);
        String dxInf    = metricas.dxTrenInferior(request.saltoLongitudCm(), sexo, edad);
        String dxGlobal = metricas.consolidarFuerza(dxCore, dxSup, dxInf);

        detalle.setRepeticionesAbdominales(request.repeticionesAbdominales());
        detalle.setRepeticionesFlexionBrazo(request.repeticionesFlexionBrazo());
        detalle.setSaltoLongitudCm(request.saltoLongitudCm());
        detalle.setDxCore(dxCore);
        detalle.setDxTrenSuperior(dxSup);
        detalle.setDxTrenInferior(dxInf);
        detalle.setDxFuerzaGlobal(dxGlobal);
    }

    private void validarEstadoEditable(EvaluacionAptitud cabecera) {
        if (cabecera.getEstadoEvaluacion() != EstadoEvaluacion.PENDIENTE) {
            throw new BusinessRuleException(
                    "No se puede registrar fuerza en una evaluación en estado "
                            + cabecera.getEstadoEvaluacion());
        }
    }

    private void validarEstadoNoAnulada(EvaluacionAptitud cabecera) {
        if (cabecera.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede actualizar fuerza en una evaluación ANULADA");
        }
    }

    private void validarNoExiste(Long idEvaluacion) {
        if (fuerzaRepository.findByEvaluacionId(idEvaluacion).isPresent()) {
            throw new BusinessRuleException(
                    "Ya existe detalle de fuerza para la evaluación: " + idEvaluacion);
        }
    }
}