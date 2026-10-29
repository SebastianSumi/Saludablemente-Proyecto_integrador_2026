package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.mapper;

import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.AlertaResumenParaEtiquetaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaClinicaResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.ParametroFotocopiaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaParametro;

import java.util.Comparator;
import java.util.List;

@Component
public class EtiquetaClinicaMapper {

    public EtiquetaClinicaResponseDto toResponseDto(
            EtiquetaClinica entity,
            List<AlertaResumenParaEtiquetaDto> alertas) {
        List<ParametroFotocopiaDto> parametros = entity.getParametros().stream()
                .sorted(Comparator.comparing(EtiquetaParametro::getParametroCodigo))
                .map(this::toParametroFotocopiaDto)
                .toList();

        return new EtiquetaClinicaResponseDto(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getColorHex(),
                entity.getEstado(),
                entity.getFechaBaja(),
                parametros,
                alertas != null ? alertas : List.of(),
                upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.RevisionAsociaciones.calcular(
                    java.util.Arrays.asList(entity.getCodigo(), entity.getNombre(), entity.getDescripcion(),
                        entity.getColorHex(), entity.getEstado(), parametros))
        );
    }

    private ParametroFotocopiaDto toParametroFotocopiaDto(EtiquetaParametro ep) {
        return new ParametroFotocopiaDto(
                ep.getIdParametro(),
                ep.getParametroCodigo(),
                ep.getParametroNombre(),
                ep.getParametroUnidad()
        );
    }
}
