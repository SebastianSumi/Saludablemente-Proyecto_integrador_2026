package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarEtiquetaRequestDto(

        @NotNull(message = "El id de la etiqueta es obligatorio")
        @Positive(message = "El id de la etiqueta debe ser positivo")
        Long idEtiqueta
) {}