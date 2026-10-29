package upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response;

import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response.AntropometricoResponse;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response.BioquimicoResponse;

import java.time.LocalDate;

public record EvaluacionResponse(
        Long idEvaluacion,
        Long idPersona,
        String idPeriodo,
        String documentoIdentidad,
        String nombresCompletos,
        Integer edadAlMomento,
        String sexoAlMomento,
        String areaTrabajoAlMomento,
        LocalDate fechaEvaluacion,
        String estadoEvaluacion,
        String observaciones,
        AntropometricoResponse detalleAntropometrico,
        BioquimicoResponse detalleBioquimico
) {}