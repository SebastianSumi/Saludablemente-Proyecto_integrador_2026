package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import java.time.LocalDateTime;

public record EtiquetaAsignadaDto(
        Long idEtiqueta,
        String codigo,
        String nombre,
        String colorHex,
        String asignadaPor,
        LocalDateTime fechaAsignacion
) {}