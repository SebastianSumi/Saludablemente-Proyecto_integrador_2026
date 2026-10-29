package upeu.edu.pe.Saludabemente.metas.meta.dto;

import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.PilarBienestar;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MetaResponseDTO(
        Long id,
        Long idPersona,
        Long idEvaluacionOrigen,
        Long idAlertaOrigen,
        Long idProfesionalPrescriptor,
        PilarBienestar pilar,
        ModalidadMeta modalidad,
        TipoOrigen tipoOrigen,
        TipoCalculo tipoCalculo,
        String indicador,
        String unidad,
        String descripcion,
        BigDecimal valorInicial,
        BigDecimal valorObjetivo,
        BigDecimal valorActual,
        LocalDate fechaInicio,
        LocalDate fechaObjetivo,
        LocalDateTime fechaCierre,
        EstadoMeta estado
) {}