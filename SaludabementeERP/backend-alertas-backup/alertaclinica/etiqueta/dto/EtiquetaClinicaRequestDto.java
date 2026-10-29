package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record EtiquetaClinicaRequestDto(

        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 40, message = "El codigo no puede superar los 40 caracteres")
        @Pattern(
            regexp = "^[A-Z][A-Z0-9_]*$",
            message = "El codigo solo admite mayusculas, numeros y guion bajo, y debe empezar por letra"
        )
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripcion no puede superar los 300 caracteres")
        String descripcion,

        @NotBlank(message = "El color es obligatorio")
        @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "El color debe estar en formato #RRGGBB (ej: #2e7d5b)"
        )
        String colorHex,

        /**
         * IDs de ParametroEvaluacion. Puede ser null o vacio
         * (etiqueta sin parametros, solo asignable manualmente).
         */
        List<@NotNull(message = "Un id de parametro no puede ser nulo")
             @Positive(message = "Los ids de parametro deben ser positivos")
             Long> idsParametros
) {}
