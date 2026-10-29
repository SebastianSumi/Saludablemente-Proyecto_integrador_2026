package upeu.edu.pe.Saludabemente.noticias.api.dto;

import java.time.LocalDateTime;

public record NoticiaFeedPublicDTO(
        Long id,
        String titulo,
        String subtitulo,
        String nombreCategoria,
        String colorCategoria,
        String imagenUrl,
        LocalDateTime fechaPublicacion
) {
}