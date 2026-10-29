package upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluacionInitRequest {

    @NotNull(message = "El idPersona es obligatorio")
    @Positive(message = "El idPersona debe ser positivo")
    private Long idPersona;

    @NotNull(message = "El idPeriodo es obligatorio")
    @Positive(message = "El idPeriodo debe ser positivo")
    private Long idPeriodoId;
}