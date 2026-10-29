package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaColesterolResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaColesterol;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaColesterolMapper {

    @Mapping(target = "idReglaColesterol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaColesterol toEntity(ReglaColesterolRequestDto requestDto);

    ReglaColesterolResponseDto toResponseDto(ReglaColesterol entity);

    List<ReglaColesterolResponseDto> toResponseDtoList(List<ReglaColesterol> entities);

    @Mapping(target = "idReglaColesterol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaColesterolRequestDto requestDto,
                             @MappingTarget ReglaColesterol entity);
}