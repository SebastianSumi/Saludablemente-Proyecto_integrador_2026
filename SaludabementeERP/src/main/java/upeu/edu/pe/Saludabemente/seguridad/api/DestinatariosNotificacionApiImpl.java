package upeu.edu.pe.Saludabemente.seguridad.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.seguridad.rol.enums.RolEnum;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinatariosNotificacionApiImpl implements DestinatariosNotificacionApi {

    private final UsuarioRepository usuarioRepository;

    /**
     * Roles de gestión que reciben todas las alertas clínicas, además del dueño.
     */
    private static final List<String> ROLES_GESTION = List.of(
            RolEnum.ADMINISTRADOR.getNombre(),
            RolEnum.COORDINADOR.getNombre(),
            RolEnum.EVALUADOR.getNombre());

    @Override
    @Transactional(readOnly = true)
    public Set<Long> resolverDestinatariosAlerta(Long idPersonaDuena, Long idUsuarioEvaluador) {
        Set<Long> destinatarios = new LinkedHashSet<>();

        // 1. Gestión: administradores, coordinadores y evaluadores activos.
        for (String rol : ROLES_GESTION) {
            usuarioRepository.findActivosByRolNombre(rol).stream()
                    .map(Usuario::getIdPersonaSnapshot)
                    .filter(id -> id != null)
                    .forEach(destinatarios::add);
        }

        // 2. Dueño de la alerta (puede no tener cuenta: se omite sin fallar).
        if (idPersonaDuena != null) {
            usuarioRepository.findByIdPersonaSnapshot(idPersonaDuena)
                    .map(Usuario::getIdPersonaSnapshot)
                    .filter(id -> id != null)
                    .ifPresentOrElse(
                            destinatarios::add,
                            () -> log.debug("Dueño sin cuenta, sin aviso In-App. idPersona={}",
                                    idPersonaDuena));
        }

        // 3. Evaluador que atiende/anula (llega como idUsuario: se traduce a idPersona).
        if (idUsuarioEvaluador != null) {
            usuarioRepository.findById(idUsuarioEvaluador)
                    .map(Usuario::getIdPersonaSnapshot)
                    .filter(id -> id != null)
                    .ifPresentOrElse(
                            destinatarios::add,
                            () -> log.debug("Evaluador sin vínculo a persona. idUsuario={}",
                                    idUsuarioEvaluador));
        }

        return destinatarios;
    }
}
