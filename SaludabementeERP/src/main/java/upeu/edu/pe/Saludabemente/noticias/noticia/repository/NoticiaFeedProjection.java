package upeu.edu.pe.Saludabemente.noticias.noticia.repository;

import java.time.LocalDateTime;

public interface NoticiaFeedProjection {
    Long getId();
    String getTitulo();
    String getSubtitulo();
    String getNombreCategoria();
    String getColorCategoria();
    String getImagenUrl();
    LocalDateTime getFechaPublicacion();
}