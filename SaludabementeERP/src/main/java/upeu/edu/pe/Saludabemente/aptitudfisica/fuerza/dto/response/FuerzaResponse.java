package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response;

import java.math.BigDecimal;

public record FuerzaResponse(
        Integer repeticionesAbdominales,
        Integer repeticionesFlexionBrazo,
        BigDecimal saltoLongitudCm,
        String dxCore,
        String dxTrenSuperior,
        String dxTrenInferior,
        String dxFuerzaGlobal
) {}