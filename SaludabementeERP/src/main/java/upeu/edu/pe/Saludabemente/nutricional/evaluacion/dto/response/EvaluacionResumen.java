package upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionResumen {

    private Long idEvaluacion;
    private Long idPersona;
    private String documentoIdentidad;
    private String nombresCompletos;
    private String areaTrabajoAlMomento;
    private String idPeriodo;
    private LocalDate fechaEvaluacion;
    private String estadoEvaluacion;
}