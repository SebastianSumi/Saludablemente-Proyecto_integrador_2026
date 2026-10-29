package upeu.edu.pe.Saludabemente.personal.persona.dto.response;

import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResumen;

import java.time.LocalDate;

public record PersonaResponse(
        Long id,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String celular,
        LocalDate fechaNacimiento,
        Integer edad,
        Sexo sexo,
        AreaResumen areaTrabajo,
        String tallaPolo,
        String estadoCivil,
        String nivelEducativo,
        TeamResumen equipo,
        Boolean activo,
        String documentoIdentidad,
        String email,
        String avatarUrl
) {
}
