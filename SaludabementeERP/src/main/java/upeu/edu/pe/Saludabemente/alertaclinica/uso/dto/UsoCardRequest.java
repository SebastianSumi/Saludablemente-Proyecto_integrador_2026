package upeu.edu.pe.Saludabemente.alertaclinica.uso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsoCardRequest(
        @NotBlank(message = "La ruta del card es obligatoria")
        @Size(max = 160, message = "La ruta del card no puede superar 160 caracteres")
        String ruta
) {}
