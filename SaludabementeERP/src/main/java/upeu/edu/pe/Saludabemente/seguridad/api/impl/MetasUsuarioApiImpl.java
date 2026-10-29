package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.util.CurrentUserProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Implementación del contrato público hacia Metas.
 *
 * <p><b>Fase 1:</b> resuelve el usuario actual desde el header
 * {@code X-Usuario-Email} vía {@link CurrentUserProvider}. Cuando JWT
 * llegue, solo cambia la dependencia — la lógica de autorización
 * permanece igual.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetasUsuarioApiImpl implements MetasUsuarioApi {

    private final UsuarioRepository usuarioRepository;
    private final CurrentUserProvider currentUserProvider;

    // -------------------------------------------------------------------
    // obtenerUsuarioActual
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UsuarioActualDTO obtenerUsuarioActual() {
        String email = currentUserProvider.obtenerEmailDesdeRequest()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no autenticado"));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + email));

        List<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombre())
                .sorted()
                .toList();

        return new UsuarioActualDTO(
                usuario.getId(),
                usuario.getIdPersonaSnapshot(),
                usuario.getEmail(),
                usuario.getNombresSnapshot(),
                roles
        );
    }

    // -------------------------------------------------------------------
    // tieneRol
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean tieneRol(String codigoRol) {
        if (codigoRol == null || codigoRol.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de rol no puede ser nulo o vacío");
        }
        return obtenerUsuarioActual().roles().contains(codigoRol);
    }

    // -------------------------------------------------------------------
    // tieneAlgunRol
    // -------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public boolean tieneAlgunRol(String... codigosRol) {
        if (codigosRol == null || codigosRol.length == 0) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos un código de rol");
        }
        for (String codigo : codigosRol) {
            if (codigo == null || codigo.isBlank()) {
                throw new IllegalArgumentException(
                        "Los códigos de rol no pueden ser nulos o vacíos");
            }
        }

        List<String> roles = obtenerUsuarioActual().roles();
        return Arrays.stream(codigosRol).anyMatch(roles::contains);
    }
}