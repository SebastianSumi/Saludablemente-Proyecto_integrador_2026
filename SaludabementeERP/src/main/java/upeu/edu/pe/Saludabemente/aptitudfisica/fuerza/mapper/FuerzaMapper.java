package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.dto.response.FuerzaResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity.DetalleFuerzaResistencia;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FuerzaMapper {

    FuerzaResponse toResponse(DetalleFuerzaResistencia entity);
}