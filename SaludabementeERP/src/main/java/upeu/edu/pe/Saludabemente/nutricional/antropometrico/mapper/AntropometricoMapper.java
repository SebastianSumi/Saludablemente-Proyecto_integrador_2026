package upeu.edu.pe.Saludabemente.nutricional.antropometrico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.AntropometricoDiagnosticosDto;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.request.AntropometricoRequest;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response.AntropometricoResponse;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.entity.DetalleAntropometrico;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AntropometricoMapper {

    /**
     * Fila 30 de PLANIFICACION: request + diagnósticos calculados → entidad.
     * El Service calcula los diagnósticos con OmronRulesUtil y los pasa
     * agrupados en un solo DTO.
     */
    @Mapping(target = "idAntropometrico", ignore = true)
    @Mapping(target = "evaluacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "estaturaCm", source = "request.estaturaCm")
    @Mapping(target = "pesoKg", source = "request.pesoKg")
    @Mapping(target = "perimetroAbdominalCm", source = "request.perimetroAbdominalCm")
    @Mapping(target = "porcentajeGrasa", source = "request.porcentajeGrasa")
    @Mapping(target = "porcentajeMasaMuscular", source = "request.porcentajeMasaMuscular")
    @Mapping(target = "porcentajeGrasaVisceral", source = "request.porcentajeGrasaVisceral")
    @Mapping(target = "imc", source = "diagnosticos.imc")
    @Mapping(target = "dxImc", source = "diagnosticos.dxImc")
    @Mapping(target = "dxPerimetroAbdominal", source = "diagnosticos.dxPerimetroAbdominal")
    @Mapping(target = "dxGrasa", source = "diagnosticos.dxGrasa")
    @Mapping(target = "dxMasaMuscular", source = "diagnosticos.dxMasaMuscular")
    @Mapping(target = "dxGrasaVisceral", source = "diagnosticos.dxGrasaVisceral")
    DetalleAntropometrico toEntity(AntropometricoRequest request,
                                   AntropometricoDiagnosticosDto diagnosticos);

    /**
     * Fila 31: entidad → response.
     */
    AntropometricoResponse toResponse(DetalleAntropometrico entity);

    /**
     * Actualización in-place de los diagnósticos sobre una entidad existente.
     * Uso: recalcular(idEvaluacion) cuando RRHH corrige la fecha de nacimiento.
     */
    @Mapping(target = "idAntropometrico", ignore = true)
    @Mapping(target = "evaluacion", ignore = true)
    @Mapping(target = "estaturaCm", ignore = true)
    @Mapping(target = "pesoKg", ignore = true)
    @Mapping(target = "perimetroAbdominalCm", ignore = true)
    @Mapping(target = "porcentajeGrasa", ignore = true)
    @Mapping(target = "porcentajeMasaMuscular", ignore = true)
    @Mapping(target = "porcentajeGrasaVisceral", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    void actualizarDiagnosticos(AntropometricoDiagnosticosDto diagnosticos,
                                @MappingTarget DetalleAntropometrico entity);
}