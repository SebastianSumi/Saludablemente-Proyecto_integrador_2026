package upeu.edu.pe.Saludabemente.metas.shared.enums;

/**
 * Ciclo de vida de una meta.
 * ACTIVA:     en curso.
 * COMPLETADA: alcanzo el 100% del objetivo. Estado terminal.
 * DESVIADA:   meta clinica sin check-in por 7 dias consecutivos. Recuperable.
 * ABANDONADA: fecha objetivo vencida sin completarse. Estado terminal.
 */
public enum EstadoMeta {
    ACTIVA,
    COMPLETADA,
    DESVIADA,
    ABANDONADA
}