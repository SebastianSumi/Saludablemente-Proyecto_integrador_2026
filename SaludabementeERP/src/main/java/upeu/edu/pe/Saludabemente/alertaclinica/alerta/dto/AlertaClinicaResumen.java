package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import lombok.Getter;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;

import java.time.LocalDateTime;

@Getter
public class AlertaClinicaResumen {

    private final Long idAlerta;
    private final Long personaId;
    private final String tipoEvaluacionOrigen;
    private final Integer puntajeTotal;
    private final String nivelSeveridad;
    private final String estado;
    private final LocalDateTime fechaGeneracion;
    private final long cantidadDetalles;

    /**
     * Constructor usado por JPQL en SELECT new.
     * Los enums llegan tal cual desde la consulta y se convierten a String con name().
     */
    public AlertaClinicaResumen(
            Long idAlerta,
            Long personaId,
            TipoEvaluacionOrigen tipoEvaluacionOrigen,
            Integer puntajeTotal,
            SeveridadAlerta nivelSeveridad,
            EstadoAlerta estado,
            LocalDateTime fechaGeneracion,
            long cantidadDetalles) {
        this.idAlerta = idAlerta;
        this.personaId = personaId;
        this.tipoEvaluacionOrigen = tipoEvaluacionOrigen.name();
        this.puntajeTotal = puntajeTotal;
        this.nivelSeveridad = nivelSeveridad.name();
        this.estado = estado.name();
        this.fechaGeneracion = fechaGeneracion;
        this.cantidadDetalles = cantidadDetalles;
    }
}