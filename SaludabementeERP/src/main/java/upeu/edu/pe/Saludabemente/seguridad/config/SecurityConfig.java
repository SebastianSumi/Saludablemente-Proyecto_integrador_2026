package upeu.edu.pe.Saludabemente.seguridad.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioRegistradoFilter usuarioRegistradoFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Autorregistro público: solo verificar y crear con rol fijo Colaborador.
                        // POST /api/v1/usuarios (con rolesIds libre) sigue exigiendo token.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/registro",
                                "/api/v1/registro/verificar",
                                "/api/v1/auth/login"
                        ).permitAll()
                        // El colaborador consulta sus recursos propios; nunca el catalogo o la bandeja global.
                        .requestMatchers(HttpMethod.GET, "/api/v1/mis-alertas", "/api/v1/mis-alertas/**").authenticated()
                        .requestMatchers("/api/v1/mis-alertas", "/api/v1/mis-alertas/**").denyAll()
                        .requestMatchers("/api/v1/etiquetas-clinicas", "/api/v1/etiquetas-clinicas/**",
                                "/api/v1/parametros-evaluacion", "/api/v1/parametros-evaluacion/**")
                            .hasAuthority("Administrador")
                        .requestMatchers(HttpMethod.GET, "/api/v1/alertas", "/api/v1/alertas/**")
                            .hasAnyAuthority("Administrador", "Evaluador", "Coordinador")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/alertas/*/atender")
                            .hasAnyAuthority("Administrador", "Evaluador")
                        .requestMatchers("/api/v1/alertas", "/api/v1/alertas/**").hasAuthority("Administrador")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                // Filtro que rechaza tokens de usuarios que no existen en Oracle.
                .addFilterAfter(usuarioRegistradoFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object roles = jwt.getClaim("roles");
            if (!(roles instanceof java.util.Collection<?>)) {
                var realmAccess = jwt.getClaimAsMap("realm_access");
                roles = realmAccess == null ? null : realmAccess.get("roles");
            }
            if (!(roles instanceof java.util.Collection<?> lista)) return List.of();
            return lista.stream().filter(String.class::isInstance).map(String.class::cast)
                    .filter(rol -> !rol.isBlank()).distinct().map(SimpleGrantedAuthority::new)
                    .map(org.springframework.security.core.GrantedAuthority.class::cast).toList();
        });
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "https://*.trycloudflare.com"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of("X-Trace-ID"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
