package upeu.edu.pe.Saludabemente.nutricional.jornada.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class JornadaEvaluacionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no debe superar 150 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a cero")
    @Max(value = 10000, message = "La capacidad no debe superar 10000")
    private Integer capacidad;

    @NotNull(message = "El periodo es obligatorio")
    @Positive(message = "El idPeriodo debe ser positivo")
    private Long idPeriodo;

    private EstadoJornada estado;
}
