package upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto;

import java.math.BigDecimal;

/**
 * Agrupador interno de resultados OMRON calculados por el Service.
 * No es DTO de entrada ni de salida: es el contrato entre
 * AntropometricoServiceImpl y AntropometricoMapper.
 *
 * Existe para que MapStruct reciba un solo objeto en lugar de 6 parámetros.
 * Resuelve DT-12 de DECISIONES.md.
 */
public record AntropometricoDiagnosticosDto(
        BigDecimal imc,
        String dxImc,
        String dxPerimetroAbdominal,
        String dxGrasa,
        String dxMasaMuscular,
        String dxGrasaVisceral
) {}