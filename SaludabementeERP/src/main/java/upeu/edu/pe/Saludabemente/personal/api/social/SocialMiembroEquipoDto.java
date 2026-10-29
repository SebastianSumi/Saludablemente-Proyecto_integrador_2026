package upeu.edu.pe.Saludabemente.personal.api.social;

public record SocialMiembroEquipoDto(Long idUsuario, Long idEquipo, String nombreEquipo,
        String logoEquipoUrl, String rolEnEquipo, boolean activo) {}
