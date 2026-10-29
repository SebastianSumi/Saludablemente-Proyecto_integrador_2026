package upeu.edu.pe.Saludabemente.personal.api;


import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;

/**
 * Resumen de persona expuesto exclusivamente al modulo Noticias
 * para validacion de autores, segmentacion por area y por equipo,
 * y conteos agregados.
 *
 * <p>No incluye datos clinicos, contacto, sexo, fecha de nacimiento ni
 * informacion administrativa (talla, estado civil, nivel educativo).</p>
 */
public record PersonaNoticiasDTO(
        Long id,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        AreaResumen areaTrabajo,       // puede ser null
        TeamSnapshotDTO team,          // puede ser null
        Boolean activo
) {
}