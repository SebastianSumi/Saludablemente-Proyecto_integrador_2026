package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonaNoticiasDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalNoticiasApiService;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalNoticiasApiServiceImpl implements PersonalNoticiasApiService {

    private final PersonaRepository personaRepository;
    private final AreaService areaService;   // ← nuevo

    // -------------------------------------------------------------------
    // Validacion
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean existeYEstaActiva(Long idPersona) {
        if (idPersona == null) return false;
        return personaRepository.existsByIdAndActivoTrue(idPersona);
    }

    // -------------------------------------------------------------------
    // Area
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<AreaResumen> obtenerAreaDePersona(Long idPersona) {
        if (idPersona == null) return Optional.empty();
        return personaRepository.findById(idPersona)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(Persona::getAreaTrabajo)
                .map(a -> new AreaResumen(a.getId(), a.getNombre()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean personaPerteneceAArea(Long idPersona, Long idArea) {
        if (idPersona == null || idArea == null) return false;
        return personaRepository.existsByIdAndAreaTrabajoIdAndActivoTrue(idPersona, idArea);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaNoticiasDTO> listarPersonasActivasPorArea(Long idArea) {
        if (idArea == null) return List.of();
        Sort sort = Sort.by(Sort.Direction.ASC, "apellidoPaterno", "apellidoMaterno", "nombres");
        return personaRepository.findByAreaTrabajoIdAndActivoTrue(idArea, sort)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPersonasActivasPorArea(Long idArea) {
        if (idArea == null) return 0L;
        return personaRepository.countByAreaTrabajoIdAndActivoTrue(idArea);
    }

    // -------------------------------------------------------------------
    // Team
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamSnapshotDTO> obtenerTeamDePersona(Long idPersona) {
        if (idPersona == null) return Optional.empty();
        return personaRepository.findById(idPersona)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(Persona::getEquipo)
                .map(this::toTeamSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean personaPerteneceATeam(Long idPersona, Long idTeam) {
        if (idPersona == null || idTeam == null) return false;
        return personaRepository.existsByIdAndEquipoIdAndActivoTrue(idPersona, idTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaNoticiasDTO> listarPersonasActivasPorTeam(Long idTeam) {
        if (idTeam == null) return List.of();
        Sort sort = Sort.by(Sort.Direction.ASC, "apellidoPaterno", "apellidoMaterno", "nombres");
        return personaRepository.findByEquipoIdAndActivoTrue(idTeam, sort)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPersonasActivasPorTeam(Long idTeam) {
        if (idTeam == null) return 0L;
        return personaRepository.countByEquipoIdAndActivoTrue(idTeam);
    }


    // -------------------------------------------------------------------
    // Mappers internos
    // -------------------------------------------------------------------

    private PersonaNoticiasDTO toDTO(Persona p) {
        AreaResumen area = p.getAreaTrabajo() != null
                ? new AreaResumen(p.getAreaTrabajo().getId(), p.getAreaTrabajo().getNombre())
                : null;
        TeamSnapshotDTO team = p.getEquipo() != null
                ? toTeamSnapshot(p.getEquipo())
                : null;
        return new PersonaNoticiasDTO(
                p.getId(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                area,
                team,
                Boolean.TRUE.equals(p.getActivo())
        );
    }

    private TeamSnapshotDTO toTeamSnapshot(Team e) {
        return new TeamSnapshotDTO(
                e.getId(),
                e.getNombre(),
                e.getDescripcion(),
                Boolean.TRUE.equals(e.getActivo())
        );
    }
}