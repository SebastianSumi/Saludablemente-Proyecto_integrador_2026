package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request;

import java.math.BigDecimal;

/**
 * Fila parseada del Excel de laboratorio.
 * No es un DTO de entrada HTTP: es el contrato interno entre
 * ExcelParserUtil y BioquimicoServiceImpl.
 *
 * Se apoya en celular como clave de cruce (ver DECISIONES.md, A-01).
 */
public record BioquimicoExcelDto(
        String celular,
        BigDecimal glucosa,
        BigDecimal colesterol,
        BigDecimal trigliceridos,
        Integer presionSistolica,
        Integer presionDiastolica,
        int numeroFila
) {}