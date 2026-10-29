package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPresionArterialResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPresionArterial;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaPresionArterialMapper {

    @Mapping(target = "idReglaPresion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaPresionArterial toEntity(ReglaPresionArterialRequestDto requestDto);

    ReglaPresionArterialResponseDto toResponseDto(ReglaPresionArterial entity);

    List<ReglaPresionArterialResponseDto> toResponseDtoList(List<ReglaPresionArterial> entities);

    @Mapping(target = "idReglaPresion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaPresionArterialRequestDto requestDto,
                             @MappingTarget ReglaPresionArterial entity);
}