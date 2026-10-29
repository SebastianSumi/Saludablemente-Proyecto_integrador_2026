package upeu.edu.pe.Saludabemente.seguridad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración mínima del módulo Seguridad.
 *
 * <p>NO habilita Spring Security (filtros, autenticación, etc.). Solo
 * expone el {@link PasswordEncoder} para el cifrado BCrypt.</p>
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}