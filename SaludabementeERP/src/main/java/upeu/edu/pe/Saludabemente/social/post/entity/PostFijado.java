package upeu.edu.pe.Saludabemente.social.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@org.hibernate.annotations.Check(constraints="(SCOPE_TIPO='GLOBAL' AND SCOPE_ID IS NULL) OR ((SCOPE_TIPO='EQUIPO' OR SCOPE_TIPO='ACTIVIDAD') AND SCOPE_ID IS NOT NULL)")
@Table(name="POST_FIJADO",schema="SLB_SOCIAL",uniqueConstraints={@UniqueConstraint(name="UK_POST_FIJADO",columnNames={"ID_POST","SCOPE_TIPO","SCOPE_ID"})})
@Getter @Setter @NoArgsConstructor
public class PostFijado {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST_FIJADO")
    private Long idPostFijado;

    @Column(name="ID_POST",nullable=false)
    private Long idPost;

    @Enumerated(EnumType.STRING)
    @Column(name="SCOPE_TIPO",nullable=false,length=20)
    private ScopeFijado scopeTipo;

    @Column(name="SCOPE_ID")
    private Long scopeId;

    @Column(name="FECHA_FIJADO",nullable=false)
    private LocalDateTime fechaFijado;

    @Column(name="ID_USUARIO_FIJADOR",nullable=false)
    private Long idUsuarioFijador;

    @Column(name="FECHA_EXPIRACION")
    private LocalDateTime fechaExpiracion;

}
