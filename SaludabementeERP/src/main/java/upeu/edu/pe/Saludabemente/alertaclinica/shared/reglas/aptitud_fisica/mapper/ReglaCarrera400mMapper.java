package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaCarrera400m;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaCarrera400mMapper {

    @Mapping(target = "idReglaCarrera", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaCarrera400m toEntity(ReglaCarrera400mRequestDto requestDto);

    ReglaCarrera400mResponseDto toResponseDto(ReglaCarrera400m entity);

    List<ReglaCarrera400mResponseDto> toResponseDtoList(List<ReglaCarrera400m> entities);

    @Mapping(target = "idReglaCarrera", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaCarrera400mRequestDto requestDto,
                             @MappingTarget ReglaCarrera400m entity);
}