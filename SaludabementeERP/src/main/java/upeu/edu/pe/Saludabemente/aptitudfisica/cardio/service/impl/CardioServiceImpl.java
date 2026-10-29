package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.request.CardioRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity.DetalleCardiovascular;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.mapper.CardioMapper;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.repository.CardioRepository;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.service.CardioService;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;
import upeu.edu.pe.Saludabemente.aptitudfisica.util.MetricasAptitudUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CardioServiceImpl implements CardioService {

    private final CardioRepository cardioRepository;
    private final CardioMapper cardioMapper;
    private final EvaluacionAptitudService evaluacionService;
    private final MetricasAptitudUtil metricas;

    @Override
    @Transactional
    public CardioResponse registrar(Long idEvaluacion, CardioRequest request) {
        EvaluacionAptitud cabecera = evaluacionService.buscarOFallar(idEvaluacion);
        validarEstadoEditable(cabecera);
        validarNoExiste(idEvaluacion);

        DetalleCardiovascular detalle = DetalleCardiovascular.builder()
                .evaluacion(cabecera)
                .build();
        aplicarMetricas(detalle, request, cabecera);

        DetalleCardiovascular guardado = cardioRepository.save(detalle);
        cabecera.setDetalleCardio(guardado);

        evaluacionService.evaluarCompletitud(idEvaluacion);

        return cardioMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public CardioResponse actualizar(Long idEvaluacion, CardioRequest request) {
        EvaluacionAptitud cabecera = evaluacionService.buscarOFallar(idEvaluacion);
        validarEstadoNoAnulada(cabecera);

        DetalleCardiovascular detalle = cardioRepository.findByEvaluacionId(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Detalle cardiovascular no encontrado para evaluación: " + idEvaluacion));

        aplicarMetricas(detalle, request, cabecera);

        DetalleCardiovascular guardado = cardioRepository.save(detalle);
        cabecera.setDetalleCardio(guardado);

        // Recalcular dictamen y fitness score con los nuevos valores
        evaluacionService.evaluarCompletitud(idEvaluacion);

        return cardioMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public CardioResponse consultar(Long idEvaluacion) {
        return cardioRepository.findByEvaluacionId(idEvaluacion)
                .map(cardioMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Detalle cardiovascular no encontrado para evaluación: " + idEvaluacion));
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private void aplicarMetricas(DetalleCardiovascular detalle, CardioRequest request,
                                 EvaluacionAptitud cabecera) {
        String dxCardio = metricas.dxCapacidadCardio(
                request.repeticionesSoga30s(),
                request.tiempoCarrera400mSeg(),
                cabecera.getSexoAlMomento(),
                cabecera.getEdadAlMomento());

        detalle.setRepeticionesSoga30s(request.repeticionesSoga30s());
        detalle.setTiempoCarrera400mSeg(request.tiempoCarrera400mSeg());
        detalle.setDxCardiovascular(dxCardio);
    }

    private void validarEstadoEditable(EvaluacionAptitud cabecera) {
        if (cabecera.getEstadoEvaluacion() != EstadoEvaluacion.PENDIENTE) {
            throw new BusinessRuleException(
                    "No se puede registrar cardio en una evaluación en estado "
                            + cabecera.getEstadoEvaluacion());
        }
    }

    private void validarEstadoNoAnulada(EvaluacionAptitud cabecera) {
        if (cabecera.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede actualizar cardio en una evaluación ANULADA");
        }
    }

    private void validarNoExiste(Long idEvaluacion) {
        if (cardioRepository.findByEvaluacionId(idEvaluacion).isPresent()) {
            throw new BusinessRuleException(
                    "Ya existe detalle cardiovascular para la evaluación: " + idEvaluacion);
        }
    }
}