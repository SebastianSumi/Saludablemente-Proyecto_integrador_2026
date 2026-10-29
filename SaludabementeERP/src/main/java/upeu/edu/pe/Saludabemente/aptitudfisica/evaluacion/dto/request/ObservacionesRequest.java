package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request;

import jakarta.validation.constraints.Size;

public record ObservacionesRequest(
        @Size(max = 500) String observaciones
) {}