package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.social.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioSnapshotDto;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;
import upeu.edu.pe.Saludabemente.teams.api.TeamPerfilDTO;
import java.util.*;

/** Composición sin importar repositorios ni entidades de Personal/Teams. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialEquipoApiImpl implements SocialEquipoApi {
    private final SocialIdentidadApi identidades;
    private final PersonaEquipoSocialApi personas;
    private final TeamApiService teams;
    private final SocialUsuarioApi usuarios;

    @Override
    public SocialEquipoSnapshotDto obtenerPorId(Long idEquipo) {
        TeamSnapshotDTO t = teams.obtenerPorId(idEquipo);
        if (t == null) throw new ResourceNotFoundException("Equipo no encontrado: " + idEquipo);
        return snapshot(t, teams.obtenerPerfil(idEquipo));
    }

    @Override
    public List<SocialEquipoSnapshotDto> obtenerPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Long> unicos = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (unicos.size() > 100) throw new BusinessRuleException("Máximo 100 equipos por lote");
        if (unicos.isEmpty()) return List.of();
        Map<Long, TeamPerfilDTO> perfiles = perfiles(teams.obtenerPerfilesPorIds(unicos));
        return teams.obtenerEquiposPorIds(unicos).stream()
                .map(t -> snapshot(t, perfiles.get(t.idTeam()))).toList();
    }

    @Override
    public SocialEquipoPerfilDto obtenerPerfil(Long idEquipo) {
        TeamPerfilDTO t = teams.obtenerPerfil(idEquipo);
        if (t == null) throw new ResourceNotFoundException("Equipo no encontrado: " + idEquipo);
        return enriquecer(List.of(t)).getFirst();
    }

    @Override
    public List<SocialEquipoPerfilDto> obtenerPerfilesPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Long> unicos = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (unicos.size() > 100) throw new BusinessRuleException("Máximo 100 equipos por lote");
        if (unicos.isEmpty()) return List.of();
        return enriquecer(teams.obtenerPerfilesPorIds(unicos));
    }

    private List<SocialEquipoPerfilDto> enriquecer(List<TeamPerfilDTO> equipos) {
        List<Long> idsLider = equipos.stream().map(TeamPerfilDTO::idUsuarioLider)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, SocialUsuarioSnapshotDto> lideres = new HashMap<>();
        for (int i = 0; i < idsLider.size(); i += 100) {
            for (SocialUsuarioSnapshotDto u : usuarios.obtenerPorIds(
                    idsLider.subList(i, Math.min(i + 100, idsLider.size())))) {
                if (idsLider.contains(u.idUsuario())) lideres.put(u.idUsuario(), u);
            }
        }
        return equipos.stream().map(t -> new SocialEquipoPerfilDto(t.idEquipo(), t.nombre(),
                t.descripcion(), t.imagenUrl(), t.color(), t.slogan(), t.referenciaBiblica(),
                t.textoVersiculo(), t.idUsuarioLider(), lider(t.idUsuarioLider(), lideres),
                Boolean.TRUE.equals(t.activo()), personas.contarPersonasActivasDeEquipo(t.idEquipo()))).toList();
    }

    private SocialLiderEquipoDto lider(Long idUsuario, Map<Long, SocialUsuarioSnapshotDto> lideres) {
        if (idUsuario == null) return null;
        SocialUsuarioSnapshotDto u = lideres.get(idUsuario);
        if (u == null || !Boolean.TRUE.equals(u.activo()))
            return new SocialLiderEquipoDto(idUsuario, "eliminado_" + idUsuario,
                    "[Usuario eliminado]", null, false);
        return new SocialLiderEquipoDto(u.idUsuario(), u.username(), u.nombreCompleto(), u.fotoUrl(), true);
    }

    private Map<Long, TeamPerfilDTO> perfiles(List<TeamPerfilDTO> lista) {
        Map<Long, TeamPerfilDTO> resultado = new HashMap<>();
        for (TeamPerfilDTO t : lista) resultado.put(t.idEquipo(), t);
        return resultado;
    }

    @Override
    public List<SocialMiembroEquipoDto> equiposDeUsuario(Long idUsuario) {
        return identidades.obtenerIdentidadActiva(idUsuario).flatMap(u ->
            personas.equipoDePersonaActiva(u.idPersona()).map(id -> {
                TeamSnapshotDTO t = teams.obtenerPorId(id);
                if (t == null || !Boolean.TRUE.equals(t.activo())) return List.<SocialMiembroEquipoDto>of();
                TeamPerfilDTO perfil = teams.obtenerPerfil(id);
                return List.of(new SocialMiembroEquipoDto(idUsuario, id, t.nombre(),
                        perfil == null ? null : perfil.imagenUrl(),
                        u.roles().contains("Coordinador") ? "ADMIN" : "MIEMBRO", true));
            })).orElse(List.of());
    }

    @Override
    public boolean esMiembroDeEquipo(Long idUsuario, Long idEquipo) {
        return idEquipo != null && equiposDeUsuario(idUsuario).stream().anyMatch(e -> idEquipo.equals(e.idEquipo()));
    }

    @Override
    public boolean esAdminDeEquipo(Long idUsuario, Long idEquipo) {
        return idEquipo != null && equiposDeUsuario(idUsuario).stream()
                .anyMatch(e -> idEquipo.equals(e.idEquipo()) && "ADMIN".equals(e.rolEnEquipo()));
    }

    @Override
    public List<SocialEquipoBusquedaDto> buscar(String query, int limite) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<TeamSnapshotDTO> encontrados = teams.listarEquiposActivos().stream()
                .filter(t -> t.nombre().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(TeamSnapshotDTO::nombre).thenComparing(TeamSnapshotDTO::idTeam))
                .limit(Math.clamp(limite, 1, 20)).toList();
        if (encontrados.isEmpty()) return List.of();
        Map<Long, TeamPerfilDTO> perfiles = perfiles(teams.obtenerPerfilesPorIds(
                encontrados.stream().map(TeamSnapshotDTO::idTeam).toList()));
        return encontrados.stream().map(t -> new SocialEquipoBusquedaDto(t.idTeam(), t.nombre(),
                perfiles.containsKey(t.idTeam()) ? perfiles.get(t.idTeam()).imagenUrl() : null)).toList();
    }

    @Override
    public List<Long> miembrosActivosDeEquipo(Long idEquipo) {
        if (idEquipo == null) return List.of();
        TeamSnapshotDTO t = teams.obtenerPorId(idEquipo);
        if (t == null || !Boolean.TRUE.equals(t.activo())) return List.of();
        List<Long> ids = personas.personasActivasDeEquipo(idEquipo);
        List<Long> usuarios = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += 100) {
            identidades.obtenerPorPersonas(ids.subList(i, Math.min(i + 100, ids.size())))
                    .stream().map(SocialIdentidadDto::idUsuario).forEach(usuarios::add);
        }
        return usuarios.stream().distinct().sorted().toList();
    }

    private SocialEquipoSnapshotDto snapshot(TeamSnapshotDTO t, TeamPerfilDTO perfil) {
        return new SocialEquipoSnapshotDto(t.idTeam(), t.nombre(), t.descripcion(),
                perfil == null ? null : perfil.imagenUrl(),
                Boolean.TRUE.equals(t.activo()), personas.contarPersonasActivasDeEquipo(t.idTeam()));
    }
}
