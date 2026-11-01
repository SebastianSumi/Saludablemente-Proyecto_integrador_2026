package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalPerfilVisualApi;
import upeu.edu.pe.Saludabemente.personal.api.PersonalPerfilVisualDTO;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialPerfilVisualDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioBusquedaDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioSnapshotDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.UsuarioSocialInactivoException;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementacion del contrato de Seguridad hacia Social.
 *
 * <p>Vive en {@code api/impl/} para que Modulith distinga claramente el
 * contrato publico de la logica interna de cada subdominio.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialUsuarioApiImpl implements SocialUsuarioApi {

    /** Jerarquia de roles, del mayor al menor. */
    private static final List<String> JERARQUIA_ROLES = List.of(
            "Administrador", "Coordinador", "Evaluador", "Colaborador"
    );

    private static final int LIMITE_MAX_BUSQUEDA = 20;

    private static final int LIMITE_BATCH_IDS = 100;

    private final UsuarioRepository usuarioRepository;
    private final PersonalPerfilVisualApi personalApi;

    @Override
    @Transactional(readOnly = true)
    public SocialPerfilVisualDto obtenerPerfilVisual(Long idUsuario) {
        return visual(personalApi.obtenerPerfilVisual(personaDeUsuario(idUsuario)));
    }

    @Override
    @Transactional
    public SocialPerfilVisualDto actualizarMiPresentacion(String presentacion) {
        Long persona = personaDeUsuario(obtenerUsuarioActual().idUsuario());
        return visual(personalApi.actualizarPresentacionSocial(persona, presentacion));
    }

    @Override
    @Transactional
    public SocialPerfilVisualDto subirMiFoto(org.springframework.web.multipart.MultipartFile archivo, boolean portada) {
        Long persona = personaDeUsuario(obtenerUsuarioActual().idUsuario());
        return visual(personalApi.subirFotoPerfilSocial(persona, archivo, portada));
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerIdPersona(Long idUsuario) {
        return personaDeUsuario(idUsuario);
    }

    private Long personaDeUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()) && u.getIdPersonaSnapshot() != null)
                .map(Usuario::getIdPersonaSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no disponible"));
    }

    private SocialPerfilVisualDto visual(PersonalPerfilVisualDTO v) {
        return new SocialPerfilVisualDto(v.fotoUrl(), v.portadaUrl(), v.presentacion());
    }

    // -------------------------------------------------------------------
    // obtenerUsuarioActual
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public SocialUsuarioActualDto obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("JWT sin claim 'email'");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + email));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsuarioSocialInactivoException(
                    "La cuenta del usuario esta inactiva");
        }

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();

        return new SocialUsuarioActualDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                roles,
                roles.contains("Administrador")
        );
    }

    // -------------------------------------------------------------------
    // obtenerPorId
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public SocialUsuarioSnapshotDto obtenerPorId(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + idUsuario));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsuarioSocialInactivoException(
                    "El usuario con id " + idUsuario + " esta inactivo");
        }

        return toSnapshot(usuario, fotos(List.of(usuario)));
    }

    // -------------------------------------------------------------------
    // obtenerPorUsername
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<SocialUsuarioSnapshotDto> obtenerPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsername(username.trim().toLowerCase())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(u -> toSnapshot(u, fotos(List.of(u))));
    }

    // -------------------------------------------------------------------
    // buscar
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SocialUsuarioBusquedaDto> buscar(String query, int limite) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int cap = Math.min(Math.max(limite, 1), LIMITE_MAX_BUSQUEDA);
        String q = query.trim();

        List<Usuario> encontrados = usuarioRepository.buscarParaMenciones(q, PageRequest.of(0, cap));
        Map<Long, String> fotos = fotos(encontrados);
        return encontrados.stream()
                .map(u -> new SocialUsuarioBusquedaDto(u.getId(), u.getUsername(),
                        armarNombreCompleto(u), fotos.get(u.getIdPersonaSnapshot())))
                .toList();
    }

    // -------------------------------------------------------------------
    // esAdministrador
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean esAdministrador(Long idUsuario) {
        if (idUsuario == null) return false;
        return usuarioRepository.findById(idUsuario)
                .map(u -> u.tieneRol("Administrador"))
                .orElse(false);
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private SocialUsuarioSnapshotDto toSnapshot(Usuario usuario, Map<Long, String> fotos) {
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();

        String rolPrincipal = JERARQUIA_ROLES.stream()
                .filter(roles::contains)
                .findFirst()
                .orElse(null);

        return new SocialUsuarioSnapshotDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                armarNombreCompleto(usuario),
                fotos.get(usuario.getIdPersonaSnapshot()),
                roles,
                rolPrincipal,
                usuario.getEquipoSnapshot(),
                usuario.getActivo()
        );
    }

    private String armarNombreCompleto(Usuario usuario) {
        StringBuilder sb = new StringBuilder()
                .append(usuario.getNombresSnapshot())
                .append(' ')
                .append(usuario.getApellidoPaternoSnapshot());
        String materno = usuario.getApellidoMaternoSnapshot();
        if (materno != null && !materno.isBlank()) {
            sb.append(' ').append(materno);
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialUsuarioSnapshotDto> obtenerPorIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // Safety cap: trunca silenciosamente a 100
        Collection<Long> idsAcotados = ids.size() > LIMITE_BATCH_IDS
                ? ids.stream().limit(LIMITE_BATCH_IDS).toList()
                : ids;

        List<Usuario> encontrados = usuarioRepository.findActivosByIdIn(idsAcotados);
        Map<Long, String> fotos = fotos(encontrados);
        return encontrados.stream().map(u -> toSnapshot(u, fotos)).toList();
    }

    private Map<Long, String> fotos(Collection<Usuario> usuarios) {
        List<Long> ids = usuarios.stream().map(Usuario::getIdPersonaSnapshot)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> resultado = new HashMap<>();
        for (int i = 0; i < ids.size(); i += LIMITE_BATCH_IDS) {
            for (var foto : personalApi.obtenerAvatares(ids.subList(i, Math.min(i + LIMITE_BATCH_IDS, ids.size())))) {
                if (foto.avatarUrl() != null && !foto.avatarUrl().isBlank()) {
                    resultado.put(foto.idPersona(), foto.avatarUrl());
                }
            }
        }
        return resultado;
    }
}
