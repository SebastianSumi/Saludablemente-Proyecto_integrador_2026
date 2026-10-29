package upeu.edu.pe.Saludabemente.social.comentario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="COMENTARIO_ADJUNTO",schema="SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class ComentarioAdjunto {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_COMENTARIO_ADJUNTO")
    private Long idComentarioAdjunto;

    @Column(name="ID_COMENTARIO",nullable=false)
    private Long idComentario;

    @Column(name="URL",nullable=false,length=500)
    private String url;

    @Column(name="ORDEN")
    private Integer orden;

    @Column(name="MIME",length=50)
    private String mime;

    @Column(name="SIZE_BYTES")
    private Long sizeBytes;

}