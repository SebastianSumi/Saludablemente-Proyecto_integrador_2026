package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.dto.ReglaPerimetroAbdominalResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPerimetroAbdominal;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReglaPerimetroAbdominalMapper {

    @Mapping(target = "idReglaPerimetro", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    ReglaPerimetroAbdominal toEntity(ReglaPerimetroAbdominalRequestDto requestDto);

    ReglaPerimetroAbdominalResponseDto toResponseDto(ReglaPerimetroAbdominal entity);

    List<ReglaPerimetroAbdominalResponseDto> toResponseDtoList(List<ReglaPerimetroAbdominal> entities);

    @Mapping(target = "idReglaPerimetro", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaBaja", ignore = true)
    void updateEntityFromDto(ReglaPerimetroAbdominalRequestDto requestDto,
                             @MappingTarget ReglaPerimetroAbdominal entity);
}