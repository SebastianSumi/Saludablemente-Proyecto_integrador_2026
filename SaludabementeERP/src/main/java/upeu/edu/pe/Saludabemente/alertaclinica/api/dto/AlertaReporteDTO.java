package upeu.edu.pe.Saludabemente.alertaclinica.api.dto;

import java.time.LocalDateTime;

/** Vista reducida de una alerta clinica para reportes personales. */
public record AlertaReporteDTO(
        Long idAlerta,
        Long personaId,
        String tipoEvaluacionOrigen,
        Integer puntajeTotal,
        String nivelSeveridad,
        String estado,
        LocalDateTime fechaGeneracion,
        long cantidadDetalles
) {}
