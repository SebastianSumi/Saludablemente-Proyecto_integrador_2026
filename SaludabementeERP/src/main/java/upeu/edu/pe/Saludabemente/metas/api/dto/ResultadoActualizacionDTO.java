package upeu.edu.pe.Saludabemente.metas.api.dto;

import java.util.List;

/**
 * Resultado de la sincronizacion de metas disparada por una evaluacion
 * nutricional completa.
 *
 * Regla de cardinalidad: metasEvaluadas >= metasActualizadas >= metasCompletadas.
 */
public record ResultadoActualizacionDTO(
        Long idPersona,
        Long idEvaluacion,
        int metasEvaluadas,
        int metasActualizadas,
        int metasCompletadas,
        List<Long> idsMetasAfectadas
) {}