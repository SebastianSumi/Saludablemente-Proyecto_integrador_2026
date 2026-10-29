package upeu.edu.pe.Saludabemente.asistencias.registro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import upeu.edu.pe.Saludabemente.asistencias.enums.MarcaAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;

@Schema(description = "Marcación manual del wizard (upsert por actividad+persona+tipo)")
public record MarcacionRequest(

        @NotNull(message = "idActividad es obligatorio")
        Long idActividad,

        @NotNull(message = "idPersona es obligatorio")
        Long idPersona,

        @NotNull(message = "marca es obligatoria")
        MarcaAsistencia marca,

        @NotNull(message = "tipoAsistencia es obligatorio")
        TipoAsistencia tipoAsistencia,

        BigDecimal latitud,

        BigDecimal longitud
) {}
