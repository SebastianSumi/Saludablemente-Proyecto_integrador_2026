package upeu.edu.pe.Saludabemente.social.comentario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="COMENTARIO_HASHTAG",schema="SLB_SOCIAL",uniqueConstraints={@UniqueConstraint(columnNames={"ID_COMENTARIO","TAG"})})
@Getter @Setter @NoArgsConstructor
public class ComentarioHashtag {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_COMENTARIO_HASHTAG")
    private Long idComentarioHashtag;

    @Column(name="ID_COMENTARIO",nullable=false)
    private Long idComentario;

    @Column(name="TAG",nullable=false,length=50)
    private String tag;

}