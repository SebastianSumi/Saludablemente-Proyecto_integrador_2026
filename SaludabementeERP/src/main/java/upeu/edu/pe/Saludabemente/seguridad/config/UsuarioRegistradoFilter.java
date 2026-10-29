package upeu.edu.pe.Saludabemente.seguridad.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;

import java.io.IOException;

/**
 * Rechaza requests cuyo JWT corresponda a un email que no exista en Oracle.
 *
 * <p>Evita que tokens emitidos para cuentas ya eliminadas puedan operar
 * contra la API. La regla es: el backend es dueño de los usuarios y de la
 * emisión de tokens.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioRegistradoFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Solo actuar si hay autenticación con JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");

            if (email != null && !usuarioRepository.existsByEmail(email)) {
                log.warn("Acceso denegado: usuario '{}' con token válido pero no registrado en Oracle", email);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Usuario no registrado en el sistema. Contacte al administrador."
                    }
                    """);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}