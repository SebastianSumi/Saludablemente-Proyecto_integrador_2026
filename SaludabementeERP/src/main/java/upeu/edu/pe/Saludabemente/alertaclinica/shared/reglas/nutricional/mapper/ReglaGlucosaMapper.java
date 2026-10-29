package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaGlucosaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGlucosa;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaGlucosaMapper {

    @Mapping(target = "idReglaGlucosa", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaGlucosa toEntity(ReglaGlucosaRequestDto requestDto);

    ReglaGlucosaResponseDto toResponseDto(ReglaGlucosa entity);

    List<ReglaGlucosaResponseDto> toResponseDtoList(List<ReglaGlucosa> entities);

    @Mapping(target = "idReglaGlucosa", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaGlucosaRequestDto requestDto, @MappingTarget ReglaGlucosa entity);
}