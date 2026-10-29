package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.dto.response.CardioResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity.DetalleCardiovascular;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardioMapper {

    CardioResponse toResponse(DetalleCardiovascular entity);
}