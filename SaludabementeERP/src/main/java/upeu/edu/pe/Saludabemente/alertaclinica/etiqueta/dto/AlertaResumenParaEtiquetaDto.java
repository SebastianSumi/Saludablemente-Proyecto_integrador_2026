package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;

import java.time.LocalDateTime;

/**
 * Resumen de una alerta asociada a una etiqueta.
 * Solo los campos necesarios para el detalle de la etiqueta.
 */
public record AlertaResumenParaEtiquetaDto(
        Long idAlerta,
        Long personaId,
        String nivelSeveridad,
        String estado,
        LocalDateTime fechaGeneracion,
        String asignadaPor,
        LocalDateTime fechaAsignacion
) {
    /**
     * Constructor que acepta enums (usado por la expresion JPQL).
     */
    public AlertaResumenParaEtiquetaDto(
            Long idAlerta,
            Long personaId,
            SeveridadAlerta nivelSeveridad,
            EstadoAlerta estado,
            LocalDateTime fechaGeneracion,
            String asignadaPor,
            LocalDateTime fechaAsignacion
    ) {
        this(
                idAlerta,
                personaId,
                nivelSeveridad != null ? nivelSeveridad.name() : null,
                estado != null ? estado.name() : null,
                fechaGeneracion,
                asignadaPor,
                fechaAsignacion
        );
    }
}
