package upeu.edu.pe.Saludabemente.social.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="POST_VISTA",schema="SLB_SOCIAL",uniqueConstraints={@UniqueConstraint(name="UK_POST_VISTA",columnNames={"ID_POST","ID_USUARIO"})})
@Getter @Setter @NoArgsConstructor
public class PostVista {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST_VISTA") private Long idPostVista;
@Column(name="ID_POST",nullable=false) private Long idPost;
@Column(name="ID_USUARIO",nullable=false) private Long idUsuario;
@Column(name="FECHA_PRIMERA_VISTA",nullable=false) private LocalDateTime fechaPrimeraVista;
@Column(name="FECHA_ULTIMA_VISTA",nullable=false) private LocalDateTime fechaUltimaVista;
}