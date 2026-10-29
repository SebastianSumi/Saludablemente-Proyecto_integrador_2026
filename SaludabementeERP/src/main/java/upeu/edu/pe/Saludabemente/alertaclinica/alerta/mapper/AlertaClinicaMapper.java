package upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaDetalleResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinicaDetalle;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.AlertaEtiqueta;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertaClinicaMapper {

    AlertaClinicaResponse toResponse(AlertaClinica entity);

    AlertaClinicaDetalleResponse toDetalleResponse(AlertaClinicaDetalle detalle);

    @Mapping(target = "codigo", source = "etiquetaCodigo")
    @Mapping(target = "nombre", source = "etiquetaNombre")
    @Mapping(target = "colorHex", source = "etiquetaColorHex")
    EtiquetaAsignadaDto toEtiquetaAsignada(AlertaEtiqueta entidad);

    @Mapping(target = "idDetalle", ignore = true)
    @Mapping(target = "alerta", ignore = true)
    @Mapping(target = "motorDetonante", constant = "VALOR_ABSOLUTO")
    AlertaClinicaDetalle toDetalle(Hallazgo hallazgo);

    List<AlertaClinicaResponse> toResponseList(List<AlertaClinica> entities);
}