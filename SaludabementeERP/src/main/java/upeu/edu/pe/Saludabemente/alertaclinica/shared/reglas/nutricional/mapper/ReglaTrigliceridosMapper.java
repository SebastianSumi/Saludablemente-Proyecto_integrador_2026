package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaTrigliceridosResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaTrigliceridos;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaTrigliceridosMapper {

    @Mapping(target = "idReglaTrigliceridos", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaTrigliceridos toEntity(ReglaTrigliceridosRequestDto requestDto);

    ReglaTrigliceridosResponseDto toResponseDto(ReglaTrigliceridos entity);

    List<ReglaTrigliceridosResponseDto> toResponseDtoList(List<ReglaTrigliceridos> entities);

    @Mapping(target = "idReglaTrigliceridos", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaTrigliceridosRequestDto requestDto,
                             @MappingTarget ReglaTrigliceridos entity);
}