package upeu.edu.pe.Saludabemente.personal.api;


import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;

import java.util.List;
import java.util.Optional;

/**
 * API publica del modulo Personal, expuesta exclusivamente al modulo
 * Noticias para validacion de autores, segmentacion por area y por equipo,
 * y conteos agregados.
 *
 * <p>Contrato de solo lectura. Ningun metodo modifica estado, expone
 * entidades ni lanza excepcion por ausencia de datos: devuelve
 * {@code false}, {@code Optional.empty()}, lista vacia o {@code 0}
 * segun corresponda.</p>
 */
public interface PersonalNoticiasApiService {

    // --- Validacion ---

    boolean existeYEstaActiva(Long idPersona);

    // --- Area ---

    Optional<AreaResumen> obtenerAreaDePersona(Long idPersona);

    boolean personaPerteneceAArea(Long idPersona, Long idArea);

    List<PersonaNoticiasDTO> listarPersonasActivasPorArea(Long idArea);

    long contarPersonasActivasPorArea(Long idArea);

    // --- Team ---

    Optional<TeamSnapshotDTO> obtenerTeamDePersona(Long idPersona);

    boolean personaPerteneceATeam(Long idPersona, Long idTeam);

    List<PersonaNoticiasDTO> listarPersonasActivasPorTeam(Long idTeam);

    long contarPersonasActivasPorTeam(Long idTeam);

    // --- Consulta puntual ---


}