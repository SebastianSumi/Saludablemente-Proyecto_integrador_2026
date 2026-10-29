package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

/**
 * Agregado para el panel "Alertas por etiqueta" del dashboard.
 * Usa los datos fotocopiados en alerta_etiqueta (no joinea con
 * etiqueta_clinica) para preservar la trazabilidad historica.
 */
public record EtiquetaAgregadoDto(
        String codigo,
        String nombre,
        String colorHex,
        long cantidad
) {}