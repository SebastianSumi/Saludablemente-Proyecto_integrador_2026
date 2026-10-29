package upeu.edu.pe.Saludabemente.social.comentario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="COMENTARIO",schema="SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_COMENTARIO")
    private Long idComentario;

    @Column(name="ID_POST",nullable=false)
    private Long idPost;

    @Column(name="ID_COMENTARIO_PADRE")
    private Long idComentarioPadre;

    @Column(name="ID_USUARIO_AUTOR",nullable=false)
    private Long idUsuarioAutor;

    @Column(name="CONTENIDO",nullable=false,length=1000)
    private String contenido;

    @Column(name="EDITADO",nullable=false)
    private Boolean editado=false;

    @Column(name="FECHA_EDICION")
    private LocalDateTime fechaEdicion;

    @Column(name="FECHA_COMENTARIO",nullable=false)
    private LocalDateTime fechaComentario;

    @Column(name="ACTIVO",nullable=false)
    private Boolean activo=true;

    @Column(name="OCULTO_MODERACION",nullable=false)
    private Boolean ocultoModeracion=false;

}
