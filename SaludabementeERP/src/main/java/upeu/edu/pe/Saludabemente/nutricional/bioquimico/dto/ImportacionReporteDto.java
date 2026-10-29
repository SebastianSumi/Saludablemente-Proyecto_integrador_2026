package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto;

import java.util.List;

/**
 * Reporte devuelto tras una importación masiva de Excel bioquímico.
 * Permite al usuario saber cuántas filas entraron y cuáles fueron rechazadas.
 */
public record ImportacionReporteDto(
        String archivoOrigen,
        String periodo,
        int totalFilas,
        int procesadas,
        int rechazadas,
        List<FilaRechazadaDto> detalleRechazos
) {}