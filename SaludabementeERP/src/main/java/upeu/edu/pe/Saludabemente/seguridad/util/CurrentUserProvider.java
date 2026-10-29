package upeu.edu.pe.Saludabemente.seguridad.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resuelve la identidad del usuario actual desde el JWT validado por
 * Spring Security.
 *
 * <p>Lee el claim {@code email} del JWT emitido por el propio backend.
 * Devuelve {@code Optional.empty()} si no hay autenticación o si el JWT
 * no contiene email.</p>
 *
 * <p>Los consumidores ({@code MetasUsuarioApiImpl}, {@code NoticiasUsuarioApiImpl})
 * no cambian: siguen llamando {@code obtenerEmailDesdeRequest()}.</p>
 */
@Component
@Slf4j
public class CurrentUserProvider {

    /**
     * Email del usuario autenticado, extraído del JWT.
     * Vacío si no hay autenticación o el token no tiene email.
     */
    public Optional<String> obtenerEmailDesdeRequest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            return Optional.empty();
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(email);
    }
}