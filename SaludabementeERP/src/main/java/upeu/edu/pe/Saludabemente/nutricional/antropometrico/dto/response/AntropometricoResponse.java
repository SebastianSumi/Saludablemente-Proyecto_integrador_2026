package upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response;

import java.math.BigDecimal;

public record AntropometricoResponse(
        Long idAntropometrico,
        BigDecimal estaturaCm,
        BigDecimal pesoKg,
        BigDecimal perimetroAbdominalCm,
        BigDecimal imc,
        BigDecimal porcentajeGrasa,
        BigDecimal porcentajeMasaMuscular,
        BigDecimal porcentajeGrasaVisceral,
        String dxImc,
        String dxPerimetroAbdominal,
        String dxGrasa,
        String dxMasaMuscular,
        String dxGrasaVisceral
) {}