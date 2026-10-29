package upeu.edu.pe.Saludabemente.nutricional.api;

import java.math.BigDecimal;

/**
 * Detalle bioquímico con diagnóstico global ya calculado.
 */
public record BioquimicoDto(
        BigDecimal glucosa,
        BigDecimal colesterol,
        BigDecimal trigliceridos,
        Integer presionSistolica,
        Integer presionDiastolica,
        String dxBioquimico
) {}