package upeu.edu.pe.Saludabemente.asistencias.util;

public enum ClasificacionGeocerca {

    /** Dentro de la tolerancia. */
    OK,

    /** Sin coordenadas en el registro o en la actividad. No se puede evaluar. */
    SIN_COORDENADAS,

    /** Fuera de la tolerancia pero dentro de un margen extendido (ej. 2x). */
    SOSPECHOSO,

    /** Muy fuera de la tolerancia. Debe ir a cuarentena. */
    FUERA
}