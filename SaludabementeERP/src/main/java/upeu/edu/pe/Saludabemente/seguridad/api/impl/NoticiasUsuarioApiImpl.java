package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.seguridad.api.noticias.NoticiasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.noticias.UsuarioNoticiasDTO;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

/**
 * Implementación del contrato público hacia Noticias.
 *
 * <p>Vive en {@code api/impl/} (no en {@code usuario/service/impl/}) para
 * que Modulith distinga claramente el contrato de la lógica interna del
 * subdominio Usuario.</p>
 *
 * <p>Todos los métodos son de solo lectura y lanzan excepción cuando el
 * dato no existe, según lo pedido por Noticias.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticiasUsuarioApiImpl implements NoticiasUsuarioApi {

    private final UsuarioRepository usuarioRepository;

    // -------------------------------------------------------------------
    // validarAutorActivo
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public void validarAutorActivo(Long idUsuario) {
        Usuario usuario = buscarOFallar(idUsuario);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessRuleException(
                    "El usuario no está activo y no puede ser autor de noticia");
        }
    }

    // -------------------------------------------------------------------
    // obtenerPorId
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UsuarioNoticiasDTO obtenerPorId(Long idUsuario) {
        return toDto(buscarOFallar(idUsuario));
    }

    // -------------------------------------------------------------------
    // obtenerPorEmail
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UsuarioNoticiasDTO obtenerPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException(
                    "Email requerido para buscar usuario");
        }

        return usuarioRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con email: " + email));
    }

    // -------------------------------------------------------------------
    // obtenerPorIdPersona
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UsuarioNoticiasDTO obtenerPorIdPersona(Long idPersona) {
        return usuarioRepository.findByIdPersonaSnapshot(idPersona)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe usuario vinculado a la persona: " + idPersona));
    }

    // -------------------------------------------------------------------
    // obtenerNombrePorId
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public String obtenerNombrePorId(Long idUsuario) {
        return nombreCompleto(buscarOFallar(idUsuario));
    }

    // -------------------------------------------------------------------
    // Helpers internos
    // -------------------------------------------------------------------

    private Usuario buscarOFallar(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + idUsuario));
    }

    private UsuarioNoticiasDTO toDto(Usuario usuario) {
        return new UsuarioNoticiasDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getIdPersonaSnapshot(),
                nombreCompleto(usuario),
                usuario.getRoles().stream()
                        .map(r -> r.getNombre())
                        .sorted()
                        .toList(),
                usuario.getActivo()
        );
    }

    private String nombreCompleto(Usuario usuario) {
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
}