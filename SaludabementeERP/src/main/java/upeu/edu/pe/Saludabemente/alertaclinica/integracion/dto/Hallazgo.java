package upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto;

import java.math.BigDecimal;

/**
 * Resultado de evaluar una metrica contra el catalogo de reglas.
 * Si el valor cae fuera de rango segun la regla aplicable,
 * el evaluador devuelve un Hallazgo. Si esta dentro de rango,
 * devuelve Optional.empty().
 *
 * El campo motorDetonante siempre vale "VALOR_ABSOLUTO" por ahora.
 */
public record Hallazgo(

        String tipoIndicador,
        BigDecimal valorMedido,
        BigDecimal valorMin,
        BigDecimal valorMax,
        String clasificacion,
        Integer puntosRiesgo,
        String mensaje,
        Long reglaId,
        String entidadRegla
) {}