package upeu.edu.pe.Saludabemente.asistencias.registro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import upeu.edu.pe.Saludabemente.asistencias.enums.MarcaAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;
import java.util.List;

/** Cabecera compartida y cambios por persona; no crea una entidad cabecera. */
public record AsistenciaManualDetalleRequest(
        @NotNull @Positive Long idActividad,
        @NotNull TipoAsistencia tipoAsistencia,
        BigDecimal latitud,
        BigDecimal longitud,
        @NotEmpty List<@NotNull @Valid Detalle> detalle
) {
    public record Detalle(
            @NotNull @Positive Long idPersona,
            @NotNull MarcaAsistencia marca
    ) {}
}
