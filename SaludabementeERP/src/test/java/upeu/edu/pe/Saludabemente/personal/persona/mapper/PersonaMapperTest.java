package upeu.edu.pe.Saludabemente.personal.persona.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonaMapperTest {

    @Autowired
    private PersonaMapper personaMapper;

    @Test
    void toResponse_calculaEdadYEmbebeEquipoResumen() {
        Team equipo = Team.builder()
                .id(1L)
                .nombre("Chirimoya")
                .build();

        Persona persona = Persona.builder()
                .id(10L)
                .nombres("Ana")
                .apellidoPaterno("Lazaro")
                .apellidoMaterno("Matallana")
                .celular("951357327")
                .fechaNacimiento(LocalDate.now().minusYears(30))
                .sexo(Sexo.F)
                .equipo(equipo)
                .activo(true)
                .build();

        PersonaResponse response = personaMapper.toResponse(persona);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.edad()).isEqualTo(30);
        assertThat(response.equipo()).isNotNull();
        assertThat(response.equipo().id()).isEqualTo(1L);
        assertThat(response.equipo().nombre()).isEqualTo("Chirimoya");
    }
}