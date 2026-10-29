package upeu.edu.pe.Saludabemente.asistencias.registro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaHistorialResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AsistenciaMapper {

    AsistenciaResponse toResponse(Asistencia entity);

    List<AsistenciaResponse> toResponseList(List<Asistencia> entities);

    @Mapping(target = "nombreActividad", source = "nombreActividad")
    @Mapping(target = "fechaActividad", source = "fechaActividad")
    AsistenciaHistorialResponse toHistorialResponse(
            Asistencia entity, String nombreActividad, LocalDateTime fechaActividad);
}