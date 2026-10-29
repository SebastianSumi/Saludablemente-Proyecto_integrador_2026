package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltaSogaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltaSoga;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaSaltaSogaMapper {

    @Mapping(target = "idReglaSaltaSoga", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaSaltaSoga toEntity(ReglaSaltaSogaRequestDto requestDto);

    ReglaSaltaSogaResponseDto toResponseDto(ReglaSaltaSoga entity);

    List<ReglaSaltaSogaResponseDto> toResponseDtoList(List<ReglaSaltaSoga> entities);

    @Mapping(target = "idReglaSaltaSoga", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaSaltaSogaRequestDto requestDto,
                             @MappingTarget ReglaSaltaSoga entity);
}