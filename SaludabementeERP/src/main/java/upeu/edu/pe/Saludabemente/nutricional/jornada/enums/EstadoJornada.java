package upeu.edu.pe.Saludabemente.nutricional.jornada.enums;

/**
 * Ciclo de vida de una jornada de evaluación nutricional.
 *
 * <p>Solo las jornadas en estado {@link #ABIERTA} admiten nuevas
 * inscripciones. El resto de estados sirven para organizar la agenda
 * sin eliminar registros históricos.</p>
 */
public enum EstadoJornada {
    PROGRAMADA,
    ABIERTA,
    CERRADA,
    CANCELADA
}
