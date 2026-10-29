package upeu.edu.pe.Saludabemente.nutricional.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.api.ContextoClinicoDTO;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionMetasApiService;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluacionMetasApiServiceImpl implements EvaluacionMetasApiService {

    private final EvaluacionRepository evaluacionRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existeEvaluacion(Long idEvaluacion, Long idPersona) {
        if (idEvaluacion == null || idPersona == null) {
            throw new IllegalArgumentException(
                    "idEvaluacion e idPersona no pueden ser null");
        }
        return evaluacionRepository.findById(idEvaluacion)
                .map(e -> e.getIdPersona().equals(idPersona))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public ContextoClinicoDTO obtenerContextoClinico(Long idEvaluacion) {
        if (idEvaluacion == null) {
            throw new IllegalArgumentException("idEvaluacion no puede ser null");
        }

        EvaluacionNutricional entity = evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación nutricional no encontrada: " + idEvaluacion));

        return new ContextoClinicoDTO(
                entity.getIdEvaluacion(),
                entity.getIdPersona(),
                entity.getFechaEvaluacion(),
                entity.getIdPeriodo(),
                construirValores(entity),
                construirDiagnosticos(entity)
        );
    }

    // -----------------------------------------------------------------
    // Mapeo de valores
    // -----------------------------------------------------------------

    private Map<String, BigDecimal> construirValores(EvaluacionNutricional e) {
        Map<String, BigDecimal> valores = new LinkedHashMap<>();

        var antro = e.getDetalleAntropometrico();
        if (antro != null) {
            putSiNoEsNulo(valores, "peso", antro.getPesoKg());
            putSiNoEsNulo(valores, "estatura", antro.getEstaturaCm());
            putSiNoEsNulo(valores, "imc", antro.getImc());
            putSiNoEsNulo(valores, "perimetroAbdominal",
                    antro.getPerimetroAbdominalCm());
            putSiNoEsNulo(valores, "porcentajeGrasa", antro.getPorcentajeGrasa());
            putSiNoEsNulo(valores, "porcentajeMasaMuscular",
                    antro.getPorcentajeMasaMuscular());
            putSiNoEsNulo(valores, "porcentajeGrasaVisceral",
                    antro.getPorcentajeGrasaVisceral());
        }

        var bio = e.getDetalleBioquimico();
        if (bio != null) {
            putSiNoEsNulo(valores, "glucosa", bio.getGlucosa());
            putSiNoEsNulo(valores, "colesterol", bio.getColesterol());
            putSiNoEsNulo(valores, "trigliceridos", bio.getTrigliceridos());

            if (bio.getPresionSistolica() != null) {
                valores.put("presionSistolica",
                        BigDecimal.valueOf(bio.getPresionSistolica()));
            }
            if (bio.getPresionDiastolica() != null) {
                valores.put("presionDiastolica",
                        BigDecimal.valueOf(bio.getPresionDiastolica()));
            }
        }

        return valores;
    }

    // -----------------------------------------------------------------
    // Mapeo de diagnósticos
    // -----------------------------------------------------------------

    private Map<String, String> construirDiagnosticos(EvaluacionNutricional e) {
        Map<String, String> diagnosticos = new LinkedHashMap<>();

        var antro = e.getDetalleAntropometrico();
        if (antro != null) {
            putSiNoEsNulo(diagnosticos, "imc", antro.getDxImc());
            putSiNoEsNulo(diagnosticos, "perimetroAbdominal",
                    antro.getDxPerimetroAbdominal());
            putSiNoEsNulo(diagnosticos, "grasa", antro.getDxGrasa());
            putSiNoEsNulo(diagnosticos, "masaMuscular", antro.getDxMasaMuscular());
            putSiNoEsNulo(diagnosticos, "grasaVisceral", antro.getDxGrasaVisceral());
        }

        var bio = e.getDetalleBioquimico();
        if (bio != null) {
            putSiNoEsNulo(diagnosticos, "bioquimico", bio.getDxBioquimico());
        }

        return diagnosticos;
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private void putSiNoEsNulo(Map<String, BigDecimal> map, String k, BigDecimal v) {
        if (v != null) map.put(k, v);
    }

    private void putSiNoEsNulo(Map<String, String> map, String k, String v) {
        if (v != null) map.put(k, v);
    }
}