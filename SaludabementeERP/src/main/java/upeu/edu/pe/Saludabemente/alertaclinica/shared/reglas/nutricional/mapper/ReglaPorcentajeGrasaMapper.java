package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPorcentajeGrasaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPorcentajeGrasa;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaPorcentajeGrasaMapper {

    @Mapping(target = "idReglaGrasa", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaPorcentajeGrasa toEntity(ReglaPorcentajeGrasaRequestDto requestDto);

    ReglaPorcentajeGrasaResponseDto toResponseDto(ReglaPorcentajeGrasa entity);

    List<ReglaPorcentajeGrasaResponseDto> toResponseDtoList(List<ReglaPorcentajeGrasa> entities);

    @Mapping(target = "idReglaGrasa", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaPorcentajeGrasaRequestDto requestDto,
                             @MappingTarget ReglaPorcentajeGrasa entity);
}