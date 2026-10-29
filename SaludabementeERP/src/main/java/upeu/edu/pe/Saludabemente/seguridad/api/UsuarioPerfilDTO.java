package upeu.edu.pe.Saludabemente.seguridad.api;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioPerfilDTO(
        Long idUsuario,
        Long idPersona,
        String email,
        String username,              // ← NUEVO
        String nombreCompleto,
        String documentoIdentidad,
        String areaTrabajo,
        String equipo,                // ← NUEVO
        Boolean activo,
        LocalDateTime fechaCreacion,
        List<String> roles
) {}