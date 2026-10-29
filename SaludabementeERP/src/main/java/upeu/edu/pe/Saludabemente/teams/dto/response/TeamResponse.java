package upeu.edu.pe.Saludabemente.teams.dto.response;

public record TeamResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        String imagenUrl,
        String color,
        String slogan,
        String referenciaBiblica,
        String textoVersiculo,
        Long idUsuarioLider
) {
}
