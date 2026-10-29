package upeu.edu.pe.Saludabemente.aptitudfisica.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Snapshot público de una evaluación física, expuesto a Recomendaciones IA.
 * Los campos de prueba son nullables: {@code null} significa "no medido",
 * no {@code 0}. {@code fechaRegistro} siempre está presente.
 */
public record AptitudFisicaDto(
        Integer abdominales30s,
        Integer planchas30s,
        Integer saltaSoga30s,
        BigDecimal saltoSinImpulsoCm,
        Integer carrera400mSeg,
        LocalDate fechaRegistro
) {}