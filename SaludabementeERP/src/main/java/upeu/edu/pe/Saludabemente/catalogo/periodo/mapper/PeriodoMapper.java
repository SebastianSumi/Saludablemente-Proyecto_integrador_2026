package upeu.edu.pe.Saludabemente.catalogo.periodo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResumen;
import upeu.edu.pe.Saludabemente.catalogo.periodo.entity.Periodo;

@Mapper(componentModel = "spring")
public interface PeriodoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "cerradoEn", ignore = true)
    @Mapping(target = "cerradoPor", ignore = true)
    Periodo toEntity(PeriodoRequest request);

    PeriodoResponse toResponse(Periodo periodo);

    PeriodoResumen toResumen(Periodo periodo);
}