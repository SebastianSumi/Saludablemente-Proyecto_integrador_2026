package upeu.edu.pe.Saludabemente.teams.service;

import org.springframework.data.domain.Sort;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResponse;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.util.List;

public interface TeamService {

    List<TeamResponse> listar(Boolean activo, Sort sort);

    TeamResponse obtener(Long id);

    TeamResponse crear(TeamRequest request);

    TeamResponse actualizar(Long id, TeamRequest request);

    void inactivar(Long id);

    Team buscarOFallar(Long id);
    TeamResponse miEquipo();
    List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> miembrosMiEquipo();
    boolean puedeEditar(Long id);
    TeamResponse asignarLider(Long id, Long idUsuario);
    List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> candidatos(Long id);
    TeamResponse subirImagen(Long id, org.springframework.web.multipart.MultipartFile archivo);
}
