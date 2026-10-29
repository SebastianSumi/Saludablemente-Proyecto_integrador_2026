package upeu.edu.pe.Saludabemente.teams.api;

/** Perfil público para consumidores internos; no expone claves de almacenamiento ni entidades. */
public record TeamPerfilDTO(Long idEquipo, String nombre, String descripcion, Boolean activo,
        String imagenUrl, String color, String slogan, String referenciaBiblica,
        String textoVersiculo, Long idUsuarioLider) {}
