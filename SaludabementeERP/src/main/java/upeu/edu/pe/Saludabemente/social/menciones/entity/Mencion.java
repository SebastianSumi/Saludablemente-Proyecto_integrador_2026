package upeu.edu.pe.Saludabemente.social.menciones.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="MENCION",schema="SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class Mencion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_MENCION") private Long idMencion;
@Enumerated(EnumType.STRING) @Column(name="TIPO_CONTENIDO",nullable=false,length=20) private TipoContenidoMencion tipoContenido;
@Column(name="ID_CONTENIDO",nullable=false) private Long idContenido;
@Enumerated(EnumType.STRING) @Column(name="TIPO_MENCION",nullable=false,length=20) private TipoMencion tipoMencion;
@Column(name="REF_ID",nullable=false) private Long refId;
@Column(name="OFFSET_INICIO") private Integer offsetInicio;
@Column(name="OFFSET_FIN") private Integer offsetFin;
}