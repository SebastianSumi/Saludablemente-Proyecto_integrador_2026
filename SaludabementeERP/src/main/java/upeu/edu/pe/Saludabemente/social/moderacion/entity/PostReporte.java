package upeu.edu.pe.Saludabemente.social.moderacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@org.hibernate.annotations.Check(constraints="(ID_POST IS NOT NULL AND ID_COMENTARIO IS NULL) OR (ID_POST IS NULL AND ID_COMENTARIO IS NOT NULL)")
@Table(name="POST_REPORTE",schema="SLB_SOCIAL",uniqueConstraints=@UniqueConstraint(name="UK_REPORTE_UNICO",columnNames="REPORTE_CLAVE"),indexes=@Index(name="IX_REPORTE_ESTADO",columnList="ESTADO,FECHA_REPORTE"))
@Getter @Setter @NoArgsConstructor
public class PostReporte {
    @Column(name="REPORTE_CLAVE",nullable=false,length=100) private String reporteClave;
    @PrePersist @PreUpdate
    void claveUnica() { reporteClave=idUsuarioReporta+":"+(idPost!=null?"P:"+idPost:"C:"+idComentario); }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST_REPORTE") private Long idPostReporte;
@Column(name="ID_POST") private Long idPost;
@Column(name="ID_COMENTARIO") private Long idComentario;
@Column(name="ID_USUARIO_REPORTA",nullable=false) private Long idUsuarioReporta;
@Enumerated(EnumType.STRING) @Column(name="MOTIVO",nullable=false,length=20) private MotivoReporte motivo;
@Column(name="DETALLE",length=500) private String detalle;
@Column(name="FECHA_REPORTE",nullable=false) private LocalDateTime fechaReporte;
@Enumerated(EnumType.STRING) @Column(name="ESTADO",nullable=false,length=20) private EstadoReporte estado=EstadoReporte.PENDIENTE;
@Column(name="ID_USUARIO_REVISOR") private Long idUsuarioRevisor;
@Column(name="FECHA_REVISION") private LocalDateTime fechaRevision;
@Column(name="ACCION_TOMADA",length=50) private String accionTomada;
@Column(name="COMENTARIO_REVISION",length=500) private String comentarioRevision;
}
