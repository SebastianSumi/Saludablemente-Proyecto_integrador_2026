package upeu.edu.pe.Saludabemente.social.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@org.hibernate.annotations.Check(constraints="((TIPO_CONTENIDO='ACTIVIDAD' OR TIPO_CONTENIDO='NOTICIA') AND REF_ID IS NOT NULL AND ORIGEN_POST_ID IS NULL) OR (TIPO_CONTENIDO='MANUAL' AND REF_ID IS NULL AND ORIGEN_POST_ID IS NULL) OR (TIPO_CONTENIDO='SHARE' AND REF_ID IS NULL AND ORIGEN_POST_ID IS NOT NULL)")
@Table(name="POST",schema="SLB_SOCIAL",uniqueConstraints={@UniqueConstraint(name="UK_POST_TIPO_REF", columnNames={"SYNC_CLAVE"}), @UniqueConstraint(name="UK_POST_AUTOR_ORIGEN", columnNames={"SHARE_CLAVE"})},indexes={@Index(name="IX_POST_MURO",columnList="ACTIVO,FECHA_PUBLICACION DESC,ID_POST DESC"),@Index(name="IX_POST_EQUIPO",columnList="ID_EQUIPO_AUTOR,ACTIVO,FECHA_PUBLICACION DESC"),@Index(name="IX_POST_AUTOR",columnList="AUTOR_ID,FECHA_PUBLICACION DESC")})
@Getter @Setter @NoArgsConstructor
public class Post {
    @Column(name="SYNC_CLAVE",length=100) private String syncClave;
    @Column(name="SHARE_CLAVE",length=100) private String shareClave;
    @PrePersist @PreUpdate
    void clavesUnicas() {
        syncClave=(tipoContenido==TipoContenidoPost.ACTIVIDAD || tipoContenido==TipoContenidoPost.NOTICIA) && refId!=null?tipoContenido.name()+":"+refId:null;
        shareClave=tipoContenido==TipoContenidoPost.SHARE && autorId!=null && origenPostId!=null?autorId+":"+origenPostId:null;
    }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST") private Long idPost;
@Enumerated(EnumType.STRING) @Column(name="TIPO_CONTENIDO",nullable=false,length=20) private TipoContenidoPost tipoContenido;
@Column(name="REF_ID") private Long refId;
@Column(name="ORIGEN_POST_ID") private Long origenPostId;
@Column(name="ID_EQUIPO_AUTOR") private Long idEquipoAutor;
@Column(name="TITULO",length=150) private String titulo;
@Column(name="RESUMEN",length=500) private String resumen;
@Column(name="LUGAR",length=150) private String lugar;
@Column(name="FECHA_EVENTO") private LocalDate fechaEvento;
@Column(name="HORA_INICIO",length=8) private String horaInicio;
@Column(name="HORA_FIN",length=8) private String horaFin;
@Column(name="REF_ESTADO",length=20) private String refEstado;
@Column(name="IMAGEN_URL",length=500) private String imagenUrl;
@Column(name="COORDINADOR_NOMBRE",length=200) private String coordinadorNombre;
@Column(name="AUTOR_ID",nullable=false) private Long autorId;
@Column(name="FECHA_PUBLICACION",nullable=false) private LocalDateTime fechaPublicacion;
@Column(name="FECHA_ACTUALIZACION") private LocalDateTime fechaActualizacion;
@Column(name="ACTIVO",nullable=false) private Boolean activo=true;
@Column(name="OCULTO_MODERACION",nullable=false) private Boolean ocultoModeracion=false;
@Column(name="ES_OFICIAL",nullable=false) private Boolean esOficial=false;
@Column(name="ALCANCE_TOTAL",nullable=false) private Long alcanceTotal=0L;
@Column(name="SHARES_TOTAL",nullable=false) private Long sharesTotal=0L;
@Version @Column(name="VERSION",nullable=false) private Long version;
}
