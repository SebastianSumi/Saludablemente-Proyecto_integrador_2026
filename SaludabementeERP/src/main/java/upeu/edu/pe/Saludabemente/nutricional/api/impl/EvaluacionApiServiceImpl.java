package upeu.edu.pe.Saludabemente.nutricional.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionApiService;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionResumenDTO;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementación del contrato público del módulo Nutricional.
 *
 * <p>Vive en {@code api.impl} — subpaquete del named interface.
 * Modulith NO lo expone porque el {@code @NamedInterface} está solo
 * en {@code nutricional.api} (el paquete padre).</p>
 */
@Service
@RequiredArgsConstructor
public class EvaluacionApiServiceImpl implements EvaluacionApiService {

    private final EvaluacionRepository evaluacionRepository;

    @Override
    @Transactional(readOnly = true)
    public EvaluacionResumenDTO obtenerResumen(Long idEvaluacion) {
        EvaluacionNutricional entity = evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación nutricional no encontrada: " + idEvaluacion));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionResumenDTO> historialPorPersona(Long idPersona) {
        return evaluacionRepository
                .findByIdPersonaOrderByFechaEvaluacionDesc(idPersona)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private EvaluacionResumenDTO toDto(EvaluacionNutricional e) {
        BigDecimal imc = null;
        String dxImc = null;
        String dxBio = null;

        if (e.getDetalleAntropometrico() != null) {
            imc = e.getDetalleAntropometrico().getImc();
            dxImc = e.getDetalleAntropometrico().getDxImc();
        }
        if (e.getDetalleBioquimico() != null) {
            dxBio = e.getDetalleBioquimico().getDxBioquimico();
        }

        return new EvaluacionResumenDTO(
                e.getIdEvaluacion(),
                e.getIdPersona(),
                e.getDocumentoIdentidad(),
                e.getNombresCompletos(),
                e.getAreaTrabajoAlMomento(),
                e.getIdPeriodo(),
                e.getFechaEvaluacion(),
                e.getEstadoEvaluacion().name(),
                imc,
                dxImc,
                dxBio
        );
    }
}