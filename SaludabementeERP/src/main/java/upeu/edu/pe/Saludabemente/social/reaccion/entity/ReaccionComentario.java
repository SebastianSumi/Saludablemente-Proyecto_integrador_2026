package upeu.edu.pe.Saludabemente.social.reaccion.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoReaccion;
@Entity @Table(name="REACCION_COMENTARIO",schema="SLB_SOCIAL")
@IdClass(ReaccionComentarioId.class)
@Getter @Setter @NoArgsConstructor
public class ReaccionComentario {

    @Id @Column(name="ID_COMENTARIO")
    private Long idComentario;

    @Id @Column(name="ID_USUARIO")
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name="TIPO",nullable=false,length=20)
    private TipoReaccion tipo;

    @Column(name="FECHA_REACCION",nullable=false)
    private LocalDateTime fechaReaccion;
}
