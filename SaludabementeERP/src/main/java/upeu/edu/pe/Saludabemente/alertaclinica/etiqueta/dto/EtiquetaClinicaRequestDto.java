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

        @io.swagger.v3.oas.annotations.media.Schema(example = "[{\"idParametro\":1},{\"idParametro\":2}]")
        @NotNull @Size(max = 500)
        List<@NotNull @jakarta.validation.Valid ParametroDetalleRequest> detalles,
        @io.swagger.v3.oas.annotations.media.Schema(description = "Omitir al crear; obligatorio al editar. Se obtiene del GET de etiqueta")
        String revision
) {}
