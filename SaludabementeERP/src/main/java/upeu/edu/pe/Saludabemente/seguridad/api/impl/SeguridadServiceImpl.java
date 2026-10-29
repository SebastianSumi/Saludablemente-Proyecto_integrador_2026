package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService;
import upeu.edu.pe.Saludabemente.seguridad.api.UsuarioPerfilDTO;
import upeu.edu.pe.Saludabemente.seguridad.api.UsuarioSnapshotDto;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Implementación del contrato público de Seguridad.
 *
 * <p>Vive en {@code api/impl/} (no en {@code usuario/service/impl/})
 * para que Modulith distinga claramente el contrato público de la
 * lógica interna del subdominio Usuario.</p>
 *
 * <p>Es la única clase del módulo Seguridad que puede exponer datos
 * a otros módulos. Todas sus dependencias son internas al módulo.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeguridadServiceImpl implements SeguridadService {

    /** Roles aceptados como coordinadores de actividades. */
    private static final Set<String> ROLES_COORDINADOR = Set.of("Coordinador", "Evaluador");

    private final UsuarioRepository usuarioRepository;

    // -------------------------------------------------------------------
    // validarCoordinador
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public void validarCoordinador(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + idUsuario));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessRuleException(
                    "El usuario no está activo y no puede ser coordinador");
        }

        boolean tieneRolValido = usuario.getRoles().stream()
                .anyMatch(r -> ROLES_COORDINADOR.contains(r.getNombre()));

        if (!tieneRolValido) {
            throw new BusinessRuleException(
                    "El usuario no tiene rol Coordinador ni Evaluador");
        }
    }

    // -------------------------------------------------------------------
    // obtenerUsuarioPorId
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UsuarioSnapshotDto obtenerUsuarioPorId(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + idUsuario));

        return toSnapshot(usuario);
    }

    // -------------------------------------------------------------------
    // listarUsuariosPorRol
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioSnapshotDto> listarUsuariosPorRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new BusinessRuleException("El nombre del rol es obligatorio");
        }

        return usuarioRepository.findActivosByRolNombre(rol.trim()).stream()
                .map(this::toSnapshot)
                .sorted(Comparator.comparing(UsuarioSnapshotDto::nombreCompleto,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // -------------------------------------------------------------------
    // validarUsuarioActivo
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean validarUsuarioActivo(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElse(false);
    }

    // -------------------------------------------------------------------
    // Mapeo interno
    // -------------------------------------------------------------------

    /**
     * Convierte la entidad {@link Usuario} a su snapshot público.
     *
     * <p>{@code nombreCompleto} se arma desde el snapshot de identidad
     * persistido en la propia tabla {@code USUARIO}. No hay llamada
     * al módulo Personal.</p>
     */
    private UsuarioSnapshotDto toSnapshot(Usuario usuario) {
        return new UsuarioSnapshotDto(
                usuario.getId(),
                usuario.getIdPersonaSnapshot(),
                construirNombreCompleto(usuario),
                usuario.getEmail(),
                usuario.getRoles().stream()
                        .map(r -> r.getNombre())
                        .sorted()
                        .toList(),
                usuario.getActivo()
        );
    }

    /**
     * Concatena nombres + apellido paterno + (apellido materno opcional).
     * Omite el apellido materno si es nulo o vacío.
     */
    private String construirNombreCompleto(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append(usuario.getNombresSnapshot())
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
    public UsuarioPerfilDTO obtenerMiPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }

        String email = jwt.getClaimAsString("email");
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + email));

        String nombreCompleto = usuario.getNombresSnapshot() + " "
                + usuario.getApellidoPaternoSnapshot()
                + (usuario.getApellidoMaternoSnapshot() != null
                ? " " + usuario.getApellidoMaternoSnapshot()
                : "");

        return new UsuarioPerfilDTO(
                usuario.getId(),
                usuario.getIdPersonaSnapshot(),
                usuario.getEmail(),
                usuario.getUsername(),                    // ← NUEVO
                nombreCompleto,
                usuario.getDocumentoIdentidadSnapshot(),
                usuario.getAreaTrabajoSnapshot(),
                usuario.getEquipoSnapshot(),              // ← NUEVO
                usuario.getActivo(),
                usuario.getFechaCreacion(),
                usuario.getRoles().stream()
                        .map(Rol::getNombre)
                        .sorted()
                        .toList()
        );
    }
}