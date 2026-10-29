package upeu.edu.pe.Saludabemente.asistencias.registro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Marcación masiva (todos PRESENTE) del wizard")
public record MarcacionLoteRequest(

        @NotNull(message = "idActividad es obligatorio")
        Long idActividad,

        @NotEmpty(message = "idsPersona es obligatorio")
        List<Long> idsPersona,

        @NotNull(message = "tipoAsistencia es obligatorio")
        TipoAsistencia tipoAsistencia,

        BigDecimal latitud,

        BigDecimal longitud
) {}
