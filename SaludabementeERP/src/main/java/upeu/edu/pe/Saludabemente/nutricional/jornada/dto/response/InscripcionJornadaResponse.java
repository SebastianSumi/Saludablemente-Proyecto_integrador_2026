package upeu.edu.pe.Saludabemente.nutricional.jornada.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionJornadaResponse {

    private Long idInscripcion;
    private Long idJornada;
    private String nombreJornada;
    private LocalDate fechaJornada;
    private LocalTime horaJornada;
    private Long idPersona;
    private String documentoIdentidad;
    private String nombresCompletos;
    private Integer numeroTurno;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estado;
}
