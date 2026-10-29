package upeu.edu.pe.Saludabemente.seguridad.auth.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String email,
        String nombre,
        List<String> roles,
        long expiraEn
) {
}
