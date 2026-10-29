package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGrasaVisceralResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGrasaVisceral;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaGrasaVisceralMapper {

    @Mapping(target = "idReglaVisceral", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaGrasaVisceral toEntity(ReglaGrasaVisceralRequestDto requestDto);

    ReglaGrasaVisceralResponseDto toResponseDto(ReglaGrasaVisceral entity);

    List<ReglaGrasaVisceralResponseDto> toResponseDtoList(List<ReglaGrasaVisceral> entities);

    @Mapping(target = "idReglaVisceral", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaGrasaVisceralRequestDto requestDto,
                             @MappingTarget ReglaGrasaVisceral entity);
}