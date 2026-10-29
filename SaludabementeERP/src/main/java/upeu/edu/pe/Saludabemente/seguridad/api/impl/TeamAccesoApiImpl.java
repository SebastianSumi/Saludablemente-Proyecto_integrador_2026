package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.personal.api.social.PersonaEquipoSocialApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeamAccesoApiImpl implements TeamAccesoApi {
    private final SocialUsuarioApi usuarios;
    private final SocialIdentidadApi identidades;
    private final PersonaEquipoSocialApi personas;

    public Long usuarioActual() { return usuarios.obtenerUsuarioActual().idUsuario(); }
    public boolean esAdministrador() { return usuarios.obtenerUsuarioActual().esAdministrador(); }
    public boolean esMiembroActivo(Long idUsuario, Long idEquipo) {
        return identidades.obtenerIdentidadActiva(idUsuario)
                .flatMap(u -> personas.equipoDePersonaActiva(u.idPersona()))
                .filter(id -> Objects.equals(id, idEquipo)).isPresent();
    }
    public Long equipoActual() {
        return identidades.obtenerIdentidadActiva(usuarioActual())
                .flatMap(u -> personas.equipoDePersonaActiva(u.idPersona())).orElse(null);
    }
    public List<CandidatoLider> candidatos(Long idEquipo) {
        List<Long> ids = personas.personasActivasDeEquipo(idEquipo);
        List<CandidatoLider> resultado = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += 100) {
            var cuentas = identidades.obtenerPorPersonas(ids.subList(i, Math.min(i + 100, ids.size())));
            var nombres = usuarios.obtenerPorIds(cuentas.stream().map(u -> u.idUsuario()).toList());
            nombres.forEach(u -> resultado.add(new CandidatoLider(u.idUsuario(), u.nombreCompleto())));
        }
        return List.copyOf(resultado);
    }
}
