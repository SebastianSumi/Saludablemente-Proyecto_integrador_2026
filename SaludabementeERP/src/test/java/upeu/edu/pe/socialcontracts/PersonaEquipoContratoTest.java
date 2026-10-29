package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.personal.api.impl.PersonaEquipoSocialApiImpl;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import upeu.edu.pe.Saludabemente.teams.entity.Team;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaEquipoContratoTest {
    @Test void membresiaEsPersonaYEquipoActivosNoSnapshotDeNombre() {
        PersonaRepository repo = mock(PersonaRepository.class);
        Team equipo = new Team(); equipo.setId(301L); equipo.setActivo(true);
        Persona persona = new Persona(); persona.setId(1002L); persona.setActivo(true); persona.setEquipo(equipo);
        when(repo.findById(1002L)).thenReturn(Optional.of(persona));
        var api = new PersonaEquipoSocialApiImpl(repo);
        assertEquals(301L, api.equipoDePersonaActiva(1002L).orElseThrow());
        persona.setActivo(false); assertTrue(api.equipoDePersonaActiva(1002L).isEmpty());
        persona.setActivo(true); equipo.setActivo(false);
        assertTrue(api.equipoDePersonaActiva(1002L).isEmpty());
        equipo.setActivo(true); persona.setEquipo(null);
        assertTrue(api.equipoDePersonaActiva(1002L).isEmpty());
        assertTrue(api.equipoDePersonaActiva(null).isEmpty());
    }

    @Test void listaYConteoUsanMembresiaDelRepositorioPropio() {
        PersonaRepository repo = mock(PersonaRepository.class);
        Persona p = new Persona(); p.setId(1002L);
        when(repo.findByEquipoIdAndActivoTrue(301L)).thenReturn(List.of(p));
        when(repo.countByEquipoIdAndActivoTrue(301L)).thenReturn(1L);
        var api = new PersonaEquipoSocialApiImpl(repo);
        assertEquals(List.of(1002L), api.personasActivasDeEquipo(301L));
        assertEquals(1L, api.contarPersonasActivasDeEquipo(301L));
        assertTrue(api.personasActivasDeEquipo(null).isEmpty());
        assertEquals(0, api.contarPersonasActivasDeEquipo(null));
    }
}
