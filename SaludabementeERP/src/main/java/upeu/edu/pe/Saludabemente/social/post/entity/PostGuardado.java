package upeu.edu.pe.Saludabemente.social.post.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="POST_GUARDADO",schema="SLB_SOCIAL")
@IdClass(PostGuardadoId.class) @Getter @Setter @NoArgsConstructor
public class PostGuardado {
    @Id @Column(name="ID_POST") private Long idPost;
    @Id @Column(name="ID_USUARIO") private Long idUsuario;
    @Column(name="FECHA_GUARDADO",nullable=false) private LocalDateTime fechaGuardado;
}
