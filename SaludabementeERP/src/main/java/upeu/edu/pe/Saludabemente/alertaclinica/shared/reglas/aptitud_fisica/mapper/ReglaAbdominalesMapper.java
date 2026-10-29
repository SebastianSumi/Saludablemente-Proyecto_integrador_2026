package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaAbdominalesResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaAbdominales;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaAbdominalesMapper {

    @Mapping(target = "idReglaAbdominales", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaAbdominales toEntity(ReglaAbdominalesRequestDto requestDto);

    ReglaAbdominalesResponseDto toResponseDto(ReglaAbdominales entity);

    List<ReglaAbdominalesResponseDto> toResponseDtoList(List<ReglaAbdominales> entities);

    @Mapping(target = "idReglaAbdominales", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaAbdominalesRequestDto requestDto,
                             @MappingTarget ReglaAbdominales entity);
}