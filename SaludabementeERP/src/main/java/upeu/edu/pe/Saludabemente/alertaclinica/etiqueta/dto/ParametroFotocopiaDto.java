package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

/**
 * Datos del parametro tal como estaba al momento de asociarlo
 * a la etiqueta. Es una fotocopia, no el parametro vivo.
 */
public record ParametroFotocopiaDto(
        Long idParametro,
        String codigo,
        String nombre,
        String unidad
) {}
