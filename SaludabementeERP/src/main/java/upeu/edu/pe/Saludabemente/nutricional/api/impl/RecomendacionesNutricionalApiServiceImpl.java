package upeu.edu.pe.Saludabemente.nutricional.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.api.AntropometricoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.BioquimicoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionHistoricaDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalDto;
import upeu.edu.pe.Saludabemente.nutricional.api.RecomendacionesNutricionalApiService;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecomendacionesNutricionalApiServiceImpl
        implements RecomendacionesNutricionalApiService {

    private final EvaluacionRepository evaluacionRepository;

    @Override
    @Transactional(readOnly = true)
    public EvaluacionNutricionalDto obtenerCabecera(Long idEvaluacion) {
        EvaluacionNutricional e = buscarOFallar(idEvaluacion);
        return toCabeceraDto(e);
    }

    @Override
    @Transactional(readOnly = true)
    public AntropometricoDto obtenerAntropometria(Long idEvaluacion) {
        EvaluacionNutricional e = buscarOFallar(idEvaluacion);

        if (e.getDetalleAntropometrico() == null) {
            throw new ResourceNotFoundException(
                    "La evaluación " + idEvaluacion + " no tiene antropometría");
        }

        var a = e.getDetalleAntropometrico();
        return new AntropometricoDto(
                a.getPesoKg(),
                a.getEstaturaCm(),
                a.getImc(),
                a.getDxImc(),
                a.getPerimetroAbdominalCm(),
                a.getDxPerimetroAbdominal(),
                a.getPorcentajeGrasa(),
                a.getDxGrasa(),
                a.getPorcentajeMasaMuscular(),
                a.getDxMasaMuscular(),
                a.getPorcentajeGrasaVisceral(),
                a.getDxGrasaVisceral()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BioquimicoDto> obtenerBioquimica(Long idEvaluacion) {
        EvaluacionNutricional e = buscarOFallar(idEvaluacion);

        if (e.getDetalleBioquimico() == null) {
            return Optional.empty();
        }

        var b = e.getDetalleBioquimico();
        return Optional.of(new BioquimicoDto(
                b.getGlucosa(),
                b.getColesterol(),
                b.getTrigliceridos(),
                b.getPresionSistolica(),
                b.getPresionDiastolica(),
                b.getDxBioquimico()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EvaluacionHistoricaDto> obtenerEvaluacionAnterior(
            Long idPersona, LocalDate fechaReferencia) {

        if (idPersona == null || fechaReferencia == null) {
            throw new IllegalArgumentException(
                    "idPersona y fechaReferencia no pueden ser null");
        }

        List<EvaluacionNutricional> historial = evaluacionRepository
                .findByIdPersonaOrderByFechaEvaluacionDesc(idPersona);

        return historial.stream()
                .filter(e -> e.getEstadoEvaluacion() != EstadoEvaluacion.ANULADA)
                .filter(e -> e.getFechaEvaluacion().isBefore(fechaReferencia))
                .filter(e -> e.getDetalleAntropometrico() != null)
                .findFirst()
                .map(this::toEvaluacionHistoricaDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaCompleta(Long idEvaluacion) {
        EvaluacionNutricional e = buscarOFallar(idEvaluacion);
        return e.getDetalleAntropometrico() != null
                && e.getDetalleBioquimico() != null;
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private EvaluacionNutricional buscarOFallar(Long idEvaluacion) {
        if (idEvaluacion == null) {
            throw new IllegalArgumentException("idEvaluacion no puede ser null");
        }
        return evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación nutricional no encontrada: " + idEvaluacion));
    }

    private EvaluacionNutricionalDto toCabeceraDto(EvaluacionNutricional e) {
        return new EvaluacionNutricionalDto(
                e.getIdEvaluacion(),
                e.getIdPersona(),
                e.getFechaEvaluacion(),
                e.getIdPeriodo(),
                e.getObservaciones()
        );
    }

    private EvaluacionHistoricaDto toEvaluacionHistoricaDto(EvaluacionNutricional e) {
        var a = e.getDetalleAntropometrico();
        AntropometricoDto antro = new AntropometricoDto(
                a.getPesoKg(),
                a.getEstaturaCm(),
                a.getImc(),
                a.getDxImc(),
                a.getPerimetroAbdominalCm(),
                a.getDxPerimetroAbdominal(),
                a.getPorcentajeGrasa(),
                a.getDxGrasa(),
                a.getPorcentajeMasaMuscular(),
                a.getDxMasaMuscular(),
                a.getPorcentajeGrasaVisceral(),
                a.getDxGrasaVisceral()
        );

        BioquimicoDto bio = null;
        if (e.getDetalleBioquimico() != null) {
            var b = e.getDetalleBioquimico();
            bio = new BioquimicoDto(
                    b.getGlucosa(),
                    b.getColesterol(),
                    b.getTrigliceridos(),
                    b.getPresionSistolica(),
                    b.getPresionDiastolica(),
                    b.getDxBioquimico()
            );
        }

        return new EvaluacionHistoricaDto(toCabeceraDto(e), antro, bio);
    }
}