package upeu.edu.pe.Saludabemente.nutricional.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Contexto clínico de una evaluación nutricional, expuesto al módulo Metas.
 *
 * <p>Las claves de {@code valores} y {@code diagnosticos} son vocabulario
 * cerrado, coordinado con Metas. No renombrar sin versionar el contrato.</p>
 *
 * <p>Las claves bioquímicas de {@code valores} están ausentes si la
 * evaluación aún no tiene importación de laboratorio.</p>
 */
public record ContextoClinicoDTO(
        Long idEvaluacion,
        Long idPersona,
        LocalDate fechaEvaluacion,
        String periodo,
        Map<String, BigDecimal> valores,
        Map<String, String> diagnosticos
) {}