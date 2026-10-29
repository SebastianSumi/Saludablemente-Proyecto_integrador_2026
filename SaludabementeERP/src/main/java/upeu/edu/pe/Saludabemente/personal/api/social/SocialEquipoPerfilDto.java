package upeu.edu.pe.Saludabemente.personal.api.social;

/** Perfil completo del equipo; lectura sin snapshots persistidos en Social. */
public record SocialEquipoPerfilDto(Long idEquipo, String nombre, String descripcion,
        String imagenUrl, String color, String slogan, String referenciaBiblica,
        String textoVersiculo, Long idUsuarioLider, SocialLiderEquipoDto lider,
        boolean activo, long totalMiembros) {}
