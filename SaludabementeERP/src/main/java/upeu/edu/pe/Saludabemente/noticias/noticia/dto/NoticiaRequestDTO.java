package upeu.edu.pe.Saludabemente.noticias.noticia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoticiaRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String titulo;

    @Size(max = 200)
    private String subtitulo;

    @Size(max = 500)
    private String resumen;

    private boolean oficial;

    private Long idEquipoAutor;

    @Size(max = 500)
    private String urlCompleta;

    @NotBlank
    private String contenido;

    @NotNull
    private Long idCategoria;

    private Long idArea;

    private List<Long> idsTeams;

    @NotNull
    private Long idUsuarioAutor;
}
