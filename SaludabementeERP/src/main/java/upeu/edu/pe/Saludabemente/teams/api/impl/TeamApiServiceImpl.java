package upeu.edu.pe.Saludabemente.teams.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import upeu.edu.pe.Saludabemente.teams.repository.TeamRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamApiServiceImpl implements TeamApiService {

    private final TeamRepository teamRepository;
    private final upeu.edu.pe.Saludabemente.teams.mapper.TeamImagenMapper imagenes;

    @Override
    @Transactional(readOnly = true)
    public upeu.edu.pe.Saludabemente.teams.api.TeamPerfilDTO obtenerPerfil(Long idEquipo) {
        if (idEquipo == null) return null;
        return teamRepository.findById(idEquipo).map(this::perfil).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<upeu.edu.pe.Saludabemente.teams.api.TeamPerfilDTO> obtenerPerfilesPorIds(List<Long> idsEquipos) {
        if (idsEquipos == null || idsEquipos.isEmpty()) return List.of();
        List<Long> ids = idsEquipos.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.size() > 100) throw new BusinessRuleException("Máximo 100 perfiles de equipo por lote");
        if (ids.isEmpty()) return List.of();
        return teamRepository.findAllById(ids).stream().map(this::perfil).toList();
    }

    private upeu.edu.pe.Saludabemente.teams.api.TeamPerfilDTO perfil(Team t) {
        return new upeu.edu.pe.Saludabemente.teams.api.TeamPerfilDTO(
                t.getId(), t.getNombre(), t.getDescripcion(), t.getActivo(), imagenes.url(t.getImagenKey()),
                t.getColor(), t.getSlogan(), t.getReferenciaBiblica(), t.getTextoVersiculo(), t.getIdUsuarioLider());
    }

    @Override
    @Transactional(readOnly = true)
    public void validarEquipos(List<Long> idsEquipos) {
        if (idsEquipos == null || idsEquipos.isEmpty()) return;

        List<Team> encontrados = teamRepository.findAllById(idsEquipos);

        if (encontrados.size() != idsEquipos.size()) {
            throw new BusinessRuleException(
                    "Uno o mas equipos no existen. Verifique los IDs enviados.");
        }

        List<Long> inactivos = encontrados.stream()
                .filter(e -> !Boolean.TRUE.equals(e.getActivo()))
                .map(Team::getId)
                .toList();

        if (!inactivos.isEmpty()) {
            throw new BusinessRuleException(
                    "Los siguientes equipos estan inactivos y no pueden asignarse: " + inactivos);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamSnapshotDTO> obtenerEquiposPorIds(List<Long> idsEquipos) {
        if (idsEquipos == null || idsEquipos.isEmpty()) return List.of();
        return teamRepository.findAllById(idsEquipos).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamSnapshotDTO> listarEquiposActivos() {
        Sort sort = Sort.by(Sort.Direction.ASC, "nombre");
        return teamRepository.findByActivoTrue(sort).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamSnapshotDTO obtenerPorId(Long idEquipo) {
        if (idEquipo == null) return null;
        return teamRepository.findById(idEquipo)
                .map(this::toSnapshot)
                .orElse(null);
    }

    private TeamSnapshotDTO toSnapshot(Team e) {
        return new TeamSnapshotDTO(
                e.getId(),
                e.getNombre(),
                e.getDescripcion(),
                Boolean.TRUE.equals(e.getActivo())
        );
    }
}
