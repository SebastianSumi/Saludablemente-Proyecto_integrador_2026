package upeu.edu.pe.Saludabemente.nutricional.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluacionResumenDTO(
        Long idEvaluacion,
        Long idPersona,
        String documentoIdentidad,
        String nombresCompletos,
        String areaTrabajo,
        String idPeriodo,
        LocalDate fechaEvaluacion,
        String estadoEvaluacion,
        BigDecimal imc,
        String dxImc,
        String dxBioquimico
) {}