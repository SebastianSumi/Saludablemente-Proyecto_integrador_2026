package upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JornadaEvaluacionResponse {

    private Long idJornada;
    private String nombre;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer capacidad;
    private Long idPeriodo;
    private String periodoCodigo;
    private EstadoJornada estado;
    private Long inscritos;
    private Long cuposDisponibles;
    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
}
