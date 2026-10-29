package upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PeriodoRequest(
        @NotBlank(message = "El codigo del periodo es obligatorio")
        @Size(max = 10, message = "El codigo no puede superar 10 caracteres")
        @Pattern(
                regexp = "^\\d{4}-[12]$",
                message = "El codigo debe tener el formato YYYY-1 o YYYY-2 (ej. 2026-1)"
        )
        String codigo,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 100, message = "La descripcion no puede superar 100 caracteres")
        String descripcion,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin
) {
}
