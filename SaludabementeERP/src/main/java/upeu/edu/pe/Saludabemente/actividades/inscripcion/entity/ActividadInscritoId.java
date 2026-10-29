package upeu.edu.pe.Saludabemente.actividades.inscripcion.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta de ActividadInscrito: (idActividad, idPersona).
 */
public class ActividadInscritoId implements Serializable {

    private Long idActividad;
    private Long idPersona;

    public ActividadInscritoId() {}

    public ActividadInscritoId(Long idActividad, Long idPersona) {
        this.idActividad = idActividad;
        this.idPersona = idPersona;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActividadInscritoId that)) return false;
        return Objects.equals(idActividad, that.idActividad)
                && Objects.equals(idPersona, that.idPersona);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idActividad, idPersona);
    }
}