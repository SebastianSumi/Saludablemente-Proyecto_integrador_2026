package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.personal.api.social.PersonaEquipoSocialApi;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonaEquipoSocialApiImpl implements PersonaEquipoSocialApi {
    private final PersonaRepository personas;

    @Override
    public Optional<Long> equipoDePersonaActiva(Long idPersona) {
        if (idPersona == null) return Optional.empty();
        return personas.findById(idPersona).filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .filter(p -> p.getEquipo() != null && Boolean.TRUE.equals(p.getEquipo().getActivo()))
                .map(p -> p.getEquipo().getId());
    }

    @Override
    public List<Long> personasActivasDeEquipo(Long idEquipo) {
        return idEquipo == null ? List.of() : personas.findByEquipoIdAndActivoTrue(idEquipo)
                .stream().map(p -> p.getId()).sorted().toList();
    }

    @Override
    public long contarPersonasActivasDeEquipo(Long idEquipo) {
        return idEquipo == null ? 0 : personas.countByEquipoIdAndActivoTrue(idEquipo);
    }
}
