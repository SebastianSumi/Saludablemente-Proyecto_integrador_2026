package upeu.edu.pe.Saludabemente.nutricional.jornada.enums;

/**
 * Estado de una inscripción a una jornada de evaluación nutricional.
 *
 * <p>Una inscripción {@link #CANCELADA} libera el cupo, pero conserva su
 * número de turno histórico para no reutilizarlo.</p>
 */
public enum EstadoInscripcion {
    ACTIVA,
    CANCELADA,
    ASISTIO
}
