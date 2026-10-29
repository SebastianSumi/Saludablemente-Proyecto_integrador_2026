package upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.MotivoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Registro retenido en cuarentena")
public record CuarentenaResponse(
        Long idCuarentena,
        String idLocal,
        Long idActividad,
        Long idPersona,
        TipoAsistencia tipoAsistencia,
        LocalDateTime horaMarcado,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal distanciaGeocercaM,
        MotivoCuarentena motivo,
        EstadoCuarentena estado,
        String observaciones,
        Long idUsuarioResuelve,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaResolucion
) {}