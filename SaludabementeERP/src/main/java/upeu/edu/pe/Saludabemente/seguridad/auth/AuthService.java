package upeu.edu.pe.Saludabemente.seguridad.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.AutenticacionException;
import upeu.edu.pe.Saludabemente.seguridad.auth.dto.LoginRequest;
import upeu.edu.pe.Saludabemente.seguridad.auth.dto.LoginResponse;
import upeu.edu.pe.Saludabemente.seguridad.config.JwtConfig;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.util.CifradoUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CifradoUtil cifradoUtil;
    private final JwtEncoder jwtEncoder;

    @Value("${saludablemente.security.jwt.ttl-minutos:60}")
    private long ttlMinutos;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identificador = request.identificador().trim();
        String normalizado = identificador.toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmail(identificador)
                .or(() -> usuarioRepository.findByUsername(identificador))
                .or(() -> usuarioRepository.findByEmail(normalizado))
                .or(() -> usuarioRepository.findByUsername(normalizado))
                .orElseThrow(() -> new AutenticacionException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AutenticacionException("La cuenta está inactiva");
        }

        if (!cifradoUtil.matches(request.password(), usuario.getPasswordHash())) {
            throw new AutenticacionException("Credenciales inválidas");
        }

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();

        Instant ahora = Instant.now();
        Instant expira = ahora.plus(ttlMinutos, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("username", usuario.getUsername())
                .claim("name", nombreCompleto(usuario))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new LoginResponse(
                token,
                usuario.getEmail(),
                nombreCompleto(usuario),
                roles,
                expira.getEpochSecond()
        );
    }

    private String nombreCompleto(Usuario usuario) {
        StringBuilder sb = new StringBuilder()
                .append(usuario.getNombresSnapshot())
                .append(' ')
                .append(usuario.getApellidoPaternoSnapshot());
        if (usuario.getApellidoMaternoSnapshot() != null
                && !usuario.getApellidoMaternoSnapshot().isBlank()) {
            sb.append(' ').append(usuario.getApellidoMaternoSnapshot());
        }
        return sb.toString().trim();
    }
}
