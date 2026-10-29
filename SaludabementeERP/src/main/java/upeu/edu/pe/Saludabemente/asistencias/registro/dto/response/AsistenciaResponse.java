package upeu.edu.pe.Saludabemente.asistencias.registro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import upeu.edu.pe.Saludabemente.asistencias.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Asistencia registrada")
public record AsistenciaResponse(
        Long idAsistencia,
        String idLocal,
        Long idActividad,
        Long idPersona,
        TipoAsistencia tipoAsistencia,
        MetodoRegistro metodoRegistro,
        EscaneadoPor escaneadoPor,
        EstadoAsistencia estado,
        LocalDateTime horaMarcado,
        LocalDateTime horaCheckIn,
        LocalDateTime horaCheckOut,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal distanciaGeocercaM,
        Boolean flagSospechoso,
        SyncStatus syncStatus,
        Long idUsuarioRegistro,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}