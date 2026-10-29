package upeu.edu.pe.Saludabemente.seguridad.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Envoltura estática del cifrado unidireccional de contraseñas.
 *
 * <p>Delegado a {@link PasswordEncoder} (BCrypt). El servicio nunca llama
 * directamente al encoder: usa este util para que la política de cifrado
 * quede centralizada en un único punto.</p>
 *
 * <p>En la fase JWT, este bean seguirá siendo la única puerta de entrada
 * al cifrado de credenciales.</p>
 */
@Component
@RequiredArgsConstructor
public class CifradoUtil {

    private final PasswordEncoder passwordEncoder;

    /**
     * Cifra una contraseña en texto plano.
     * El resultado (hash BCrypt) es irreversible.
     */
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     * Uso previsto: login (fase JWT). En esta fase no se invoca todavía.
     */
    public boolean matches(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }
}