package upeu.edu.pe.Saludabemente.recomendacionesia.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECOMENDACION_IA", schema = "SLB_RECOMENDACIONES_IA")
@Getter
@Setter
@NoArgsConstructor
public class RecomendacionIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RECOMENDACION")
    private Long idRecomendacion;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "ID_EVALUACION", nullable = false)
    private Long idEvaluacion;

    @Column(name = "TIPO", nullable = false, length = 20)
    private String tipo;

    @Column(name = "CONTENIDO_GENERADO", columnDefinition = "text")
    private String contenidoGenerado;

    @Column(name = "PROMPT_CONTEXTO", columnDefinition = "text")
    private String promptContexto;

    @Column(name = "MODELO_IA_USADO", length = 60)
    private String modeloIaUsado;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "VIGENTE", nullable = false)
    private Integer vigente;
}