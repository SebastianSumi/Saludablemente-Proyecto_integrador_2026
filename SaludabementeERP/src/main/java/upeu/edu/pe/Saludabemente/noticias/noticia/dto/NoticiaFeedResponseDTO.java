package upeu.edu.pe.Saludabemente.noticias.noticia.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticiaFeedResponseDTO {
    private Long id;
    private String titulo;
    private String subtitulo;
    private String nombreCategoria;
    private String colorCategoria;
    private String imagenUrl;
    private LocalDateTime fechaPublicacion;
}