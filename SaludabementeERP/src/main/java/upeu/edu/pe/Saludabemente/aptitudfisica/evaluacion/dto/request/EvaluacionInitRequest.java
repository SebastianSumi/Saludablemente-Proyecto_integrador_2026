package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record EvaluacionInitRequest(
        @NotNull Long idPersona,
        @NotNull Long idPeriodo,
        @NotNull @PastOrPresent LocalDate fechaEvaluacion
) {}