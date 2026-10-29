package upeu.edu.pe.Saludabemente.teams.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResponse;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResumen;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

@Mapper(componentModel = "spring", uses = TeamImagenMapper.class)
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "imagenKey", ignore = true)
    @Mapping(target = "idUsuarioLider", ignore = true)
    @Mapping(target = "version", ignore = true)
    Team toEntity(TeamRequest request);

    @Mapping(target = "imagenUrl", source = "imagenKey", qualifiedByName = "teamImagenUrl")
    TeamResponse toResponse(Team team);

    TeamResumen toResumen(Team team);
}
