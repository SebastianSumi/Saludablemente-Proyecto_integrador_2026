package upeu.edu.pe.Saludabemente.personal.api.social;

import java.util.List;
import java.util.Optional;

/** Personal es dueño de la membresía por persona, no de la identidad de cuenta. */
public interface PersonaEquipoSocialApi {
    Optional<Long> equipoDePersonaActiva(Long idPersona);
    List<Long> personasActivasDeEquipo(Long idEquipo);
    long contarPersonasActivasDeEquipo(Long idEquipo);
}
