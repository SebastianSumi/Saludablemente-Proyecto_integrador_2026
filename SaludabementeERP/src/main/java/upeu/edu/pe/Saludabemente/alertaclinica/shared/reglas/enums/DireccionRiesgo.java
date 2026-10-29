package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums;

/**
 * Indica en qué direccion del rango [min, max] esta el riesgo
 * clinico de una prueba fisica.
 *
 * ABAJO  : un valor por debajo del minimo es riesgoso.
 * ARRIBA : un valor por encima del maximo es riesgoso.
 * AMBOS  : cualquier valor fuera del rango es riesgoso.
 */
public enum DireccionRiesgo {
    ABAJO,
    ARRIBA,
    AMBOS
}