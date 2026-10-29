package upeu.edu.pe.Saludabemente.noticias.noticia.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTICIA", schema = "SLB_NOTICIAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticiaEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTICIA")
    private Long id;

    @Column(name = "TITULO", length = 150, nullable = false)
    private String titulo;

    @Column(name = "SUBTITULO", length = 200)
    private String subtitulo;

    @Column(name = "RESUMEN", length = 500)
    private String resumen;

    @Column(name = "OFICIAL", nullable = false)
    @Builder.Default
    private Boolean oficial = false;

    @Column(name = "ID_EQUIPO_AUTOR")
    private Long idEquipoAutor;

    @Column(name = "URL_COMPLETA", length = 500)
    private String urlCompleta;

    @Lob
    @Column(name = "CONTENIDO", nullable = false)
    private String contenido;

    @Column(name = "IMAGEN_URL", length = 250)
    private String imagenUrl;

    @Column(name = "ID_CATEGORIA", nullable = false)
    private Long idCategoria;

    @Column(name = "ID_AREA")
    private Long idArea;

    @Column(name = "ID_USUARIO_AUTOR", nullable = false)
    private Long idUsuarioAutor;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20, nullable = false)
    @Builder.Default
    private EstadoNoticia estado = EstadoNoticia.BORRADOR;

    @Column(name = "FECHA_PUBLICACION")
    private LocalDateTime fechaPublicacion;
}
