package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaMasaMuscularResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaMasaMuscular;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaMasaMuscularMapper {

    @Mapping(target = "idReglaMusculo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaMasaMuscular toEntity(ReglaMasaMuscularRequestDto requestDto);

    ReglaMasaMuscularResponseDto toResponseDto(ReglaMasaMuscular entity);

    List<ReglaMasaMuscularResponseDto> toResponseDtoList(List<ReglaMasaMuscular> entities);

    @Mapping(target = "idReglaMusculo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaMasaMuscularRequestDto requestDto,
                             @MappingTarget ReglaMasaMuscular entity);
}