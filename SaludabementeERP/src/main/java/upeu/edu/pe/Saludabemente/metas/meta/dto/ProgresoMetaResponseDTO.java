package upeu.edu.pe.Saludabemente.metas.meta.dto;

import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.PilarBienestar;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgresoMetaResponseDTO(
        Long idMeta,
        Long idPersona,
        String descripcion,
        PilarBienestar pilar,
        ModalidadMeta modalidad,
        TipoOrigen tipoOrigen,
        EstadoMeta estado,
        String indicador,
        String unidad,
        BigDecimal valorInicial,
        BigDecimal valorObjetivo,
        BigDecimal valorActual,
        BigDecimal porcentajeAvance,
        int rachaActual,
        long totalCheckIns,
        LocalDate fechaInicio,
        LocalDate fechaObjetivo,
        LocalDate ultimoCheckIn,
        boolean desviada
) {}