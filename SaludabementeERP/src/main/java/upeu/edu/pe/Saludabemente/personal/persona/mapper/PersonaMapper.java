package upeu.edu.pe.Saludabemente.personal.persona.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    /**
     * Request + Equipo resuelto → Entity.
     * El service resuelve el Equipo antes de llamar (regla de la guía S3).
     *
     * id: lo genera Oracle (IDENTITY).
     * activo: lo setea el service al crear.
     * equipo: viene como segundo parámetro, no del request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "equipo", source = "equipo")
    @Mapping(target = "areaTrabajo", source = "areaTrabajo")
    Persona toEntity(PersonaRequest request, Team equipo, AreaTrabajo areaTrabajo);

    /**
     * Request + Team resuelto → actualiza una Entity existente.
     * El id y activo se respetan (no se sobrescriben).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "equipo", source = "equipo")
    @Mapping(target = "areaTrabajo", source = "areaTrabajo")
    void updateEntity(PersonaRequest request, Team equipo, AreaTrabajo areaTrabajo, @MappingTarget Persona persona);

    /**
     * Entity → Response completo.
     * La edad se calcula con el método calcularEdad(...) marcado @Named.
     */
    @Mapping(target = "edad", source = "fechaNacimiento", qualifiedByName = "calcularEdad")
    @Mapping(target = "equipo", expression = "java(mapTeamToResumen(persona.getEquipo()))")
    @Mapping(target = "areaTrabajo", expression = "java(mapAreaToResumen(persona.getAreaTrabajo()))")
    PersonaResponse toResponse(Persona persona);

    /**
     * Entity → Resumen.
     * Campos coincidentes por nombre; MapStruct no necesita más.
     */
    PersonaResumen toResumen(Persona persona);

    default upeu.edu.pe.Saludabemente.teams.dto.response.TeamResumen mapTeamToResumen(Team team) {
        if (team == null) return null;
        return new upeu.edu.pe.Saludabemente.teams.dto.response.TeamResumen(team.getId(), team.getNombre());
    }

    default upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen mapAreaToResumen(AreaTrabajo area) {
        if (area == null) return null;
        return new upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen(area.getId(), area.getNombre());
    }

    /**
     * Método auxiliar: calcula la edad a partir de la fecha de nacimiento.
     * Marcado @Named para que MapStruct lo referencie desde qualifiedByName.
     * Es default (con cuerpo) porque MapStruct no genera su implementación.
     */
    @Named("calcularEdad")
    default Integer calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}