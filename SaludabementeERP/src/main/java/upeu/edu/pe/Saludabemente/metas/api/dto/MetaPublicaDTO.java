package upeu.edu.pe.Saludabemente.metas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resumen publico de una meta especifica.
 * Consumido por: exportacion, perfil.
 */
public record MetaPublicaDTO(
        Long idMeta,
        Long idPersona,
        String descripcion,
        String pilar,
        String tipoOrigen,
        String estado,
        BigDecimal valorObjetivo,
        BigDecimal valorActual,
        BigDecimal porcentajeAvance,
        LocalDate fechaInicio,
        LocalDate fechaObjetivo,
        LocalDate ultimoCheckIn
) {}