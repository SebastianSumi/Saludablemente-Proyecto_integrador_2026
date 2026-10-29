package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaPlanchas;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaPlanchasMapper {

    @Mapping(target = "idReglaPlanchas", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaPlanchas toEntity(ReglaPlanchasRequestDto requestDto);

    ReglaPlanchasResponseDto toResponseDto(ReglaPlanchas entity);

    List<ReglaPlanchasResponseDto> toResponseDtoList(List<ReglaPlanchas> entities);

    @Mapping(target = "idReglaPlanchas", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaPlanchasRequestDto requestDto,
                             @MappingTarget ReglaPlanchas entity);
}