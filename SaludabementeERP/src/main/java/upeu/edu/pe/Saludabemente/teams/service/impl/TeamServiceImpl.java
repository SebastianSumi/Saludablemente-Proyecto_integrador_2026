package upeu.edu.pe.Saludabemente.teams.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResponse;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import upeu.edu.pe.Saludabemente.teams.mapper.TeamMapper;
import upeu.edu.pe.Saludabemente.teams.repository.TeamRepository;
import upeu.edu.pe.Saludabemente.teams.service.TeamService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi acceso;
    private final upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService imagenes;

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> listar(Boolean activo, Sort sort) {
        List<Team> equipos;
        if (activo == null) {
            equipos = teamRepository.findAll(sort);
        } else if (activo) {
            equipos = teamRepository.findByActivoTrue(sort);
        } else {
            equipos = teamRepository.findByActivoFalse(sort);
        }
        return equipos.stream().map(teamMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse obtener(Long id) {
        return teamMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public TeamResponse crear(TeamRequest request) {
        exigirAdministrador();
        if (teamRepository.existsByNombreAndActivoTrue(request.nombre())) {
            throw new BusinessRuleException(
                    "Ya existe un equipo activo con el nombre: " + request.nombre());
        }
        Team team = teamMapper.toEntity(request);
        team.setActivo(true);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse actualizar(Long id, TeamRequest request) {
        Team team = buscarOFallar(id);
        exigirEditor(team);
        boolean cambiaNombre = !team.getNombre().equals(request.nombre());
        if (cambiaNombre && teamRepository.existsByNombreAndActivoTrue(request.nombre())) {
            throw new BusinessRuleException(
                    "Ya existe un equipo activo con el nombre: " + request.nombre());
        }
        team.setNombre(request.nombre());
        team.setDescripcion(request.descripcion());
        team.setColor(request.color());
        team.setSlogan(request.slogan());
        team.setReferenciaBiblica(request.referenciaBiblica());
        team.setTextoVersiculo(request.textoVersiculo());
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void inactivar(Long id) {
        exigirAdministrador();
        Team team = buscarOFallar(id);
        if (!Boolean.TRUE.equals(team.getActivo())) {
            throw new BusinessRuleException("El equipo ya está inactivo");
        }
        team.setActivo(false);
        teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public Team buscarOFallar(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    private void exigirAdministrador() {
        if (!acceso.esAdministrador()) throw new org.springframework.security.access.AccessDeniedException("Solo administrador");
    }

    private void exigirEditor(Team team) {
        if (!Boolean.TRUE.equals(team.getActivo())) throw new BusinessRuleException("El equipo está inactivo");
        if (acceso.esAdministrador()) return;
        Long usuario = acceso.usuarioActual();
        if (!java.util.Objects.equals(usuario, team.getIdUsuarioLider()) || !acceso.esMiembroActivo(usuario, team.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Solo el líder de este equipo puede editarlo");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeEditar(Long id) {
        Team team = buscarOFallar(id);
        if (!Boolean.TRUE.equals(team.getActivo())) return false;
        Long usuario = acceso.usuarioActual();
        return acceso.esAdministrador() || (java.util.Objects.equals(usuario, team.getIdUsuarioLider())
                && acceso.esMiembroActivo(usuario, id));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse miEquipo() {
        Long id = acceso.equipoActual();
        if (id == null) throw new ResourceNotFoundException("No tienes un equipo activo asignado");
        Team team = buscarOFallar(id);
        if (!Boolean.TRUE.equals(team.getActivo())) throw new ResourceNotFoundException("Tu equipo está inactivo");
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> miembrosMiEquipo() {
        return acceso.candidatos(miEquipo().id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> candidatos(Long id) {
        exigirAdministrador();
        buscarOFallar(id);
        return acceso.candidatos(id);
    }

    @Override
    @Transactional
    public TeamResponse asignarLider(Long id, Long idUsuario) {
        exigirAdministrador();
        Team team = buscarOFallar(id);
        if (!Boolean.TRUE.equals(team.getActivo())) throw new BusinessRuleException("El equipo está inactivo");
        if (idUsuario != null && !acceso.esMiembroActivo(idUsuario, id))
            throw new BusinessRuleException("El líder debe tener cuenta activa y pertenecer al equipo");
        team.setIdUsuarioLider(idUsuario);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse subirImagen(Long id, org.springframework.web.multipart.MultipartFile archivo) {
        Team team = buscarOFallar(id);
        exigirEditor(team);
        TeamImagenValidator.validar(archivo);
        String anterior = team.getImagenKey();
        String nueva = imagenes.guardar(archivo, "teams/" + id + "/logo", 5L * 1024 * 1024);
        // El almacenamiento no participa en la transacción Oracle: compensar al terminar.
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    imagenes.eliminar(status == STATUS_COMMITTED ? anterior : nueva);
                }
            });
        team.setImagenKey(nueva);
        return teamMapper.toResponse(teamRepository.saveAndFlush(team));
    }
}
