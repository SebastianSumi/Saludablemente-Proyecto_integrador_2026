package upeu.edu.pe.Saludabemente.noticias.noticia.dto;

import lombok.*;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticiaResponseDTO {
    private Long id;
    private String titulo;
    private String subtitulo;
    private String resumen;
    private Boolean oficial;
    private Long idEquipoAutor;
    private String urlCompleta;
    private String contenido;
    private String imagenUrl;
    private CategoriaResponseDTO categoria;
    private AreaResumen area;
    private List<TeamSnapshotDTO> teams;
    private String nombreAutor;
    private String estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
