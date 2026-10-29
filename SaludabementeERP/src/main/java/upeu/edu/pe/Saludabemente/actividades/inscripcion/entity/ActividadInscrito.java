package upeu.edu.pe.Saludabemente.actividades.inscripcion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Persona inscrita a una actividad.
 * Solo las personas con una fila explicita aqui pueden marcar asistencia;
 * si la actividad no tiene inscritos, no se registra ni cierra asistencia.
 */
@Entity
@Table(name = "ACTIVIDAD_INSCRITO", schema = "SLB_ACTIVIDADES")
@IdClass(ActividadInscritoId.class)
@Getter
@Setter
@NoArgsConstructor
public class ActividadInscrito {

    @Id
    @Column(name = "ID_ACTIVIDAD", nullable = false)
    private Long idActividad;

    @Id
    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "FECHA_INSCRIPCION", nullable = false)
    private LocalDateTime fechaInscripcion;

    /**
     * true si fue inscrito via "inscribir a todos".
     * false si fue inscrito manualmente uno por uno.
     */
    @Column(name = "INSCRITO_POR_TODOS", nullable = false)
    private Boolean inscritoPorTodos;

    public ActividadInscrito(Long idActividad, Long idPersona, Boolean inscritoPorTodos) {
        this.idActividad = idActividad;
        this.idPersona = idPersona;
        this.fechaInscripcion = LocalDateTime.now();
        this.inscritoPorTodos = inscritoPorTodos;
    }
}