package upeu.edu.pe.Saludabemente.teams.api;

import java.util.List;

/**
 * Contrato público del módulo Teams hacia otros módulos del sistema.
 */
public interface TeamApiService {
    /** Perfil con identidad visual; null si no existe. */
    TeamPerfilDTO obtenerPerfil(Long idEquipo);

    /** Máximo 100 IDs distintos; omite ausentes. Indexar por idEquipo, sin asumir orden. */
    List<TeamPerfilDTO> obtenerPerfilesPorIds(List<Long> idsEquipos);

    void validarEquipos(List<Long> idsEquipos);

    List<TeamSnapshotDTO> obtenerEquiposPorIds(List<Long> idsEquipos);

    List<TeamSnapshotDTO> listarEquiposActivos();

    TeamSnapshotDTO obtenerPorId(Long idEquipo);
}
