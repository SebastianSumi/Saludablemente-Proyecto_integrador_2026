package upeu.edu.pe.Saludabemente.alertaclinica.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista resumida de una alerta clinica para consumo de otros modulos.
 * No expone la entidad AlertaClinica ni sus relaciones JPA.
 *
 * La lista `indicadores` nunca viene vacia: una alerta siempre tiene
 * al menos un detalle asociado (el motor no genera alertas sin hallazgos).
 */
public record ResumenAlertaDTO(

        Long idAlerta,
        Long idPersona,
        Long idEvaluacionOrigen,
        List<String> indicadores,
        String severidad,
        String estado,
        LocalDateTime fechaGeneracion
) {}