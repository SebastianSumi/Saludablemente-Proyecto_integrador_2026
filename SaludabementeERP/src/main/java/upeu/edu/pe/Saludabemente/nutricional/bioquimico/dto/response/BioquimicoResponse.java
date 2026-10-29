package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response;

import java.math.BigDecimal;

public record BioquimicoResponse(
        Long idBioquimico,
        BigDecimal glucosa,
        BigDecimal colesterol,
        BigDecimal trigliceridos,
        Integer presionSistolica,
        Integer presionDiastolica,
        String dxBioquimico,
        String archivoOrigen
) {}