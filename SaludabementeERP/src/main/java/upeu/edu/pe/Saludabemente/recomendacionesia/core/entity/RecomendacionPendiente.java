package upeu.edu.pe.Saludabemente.recomendacionesia.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECOMENDACION_PENDIENTE", schema = "SLB_RECOMENDACIONES_IA")
@Getter
@Setter
@NoArgsConstructor
public class RecomendacionPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PENDIENTE")
    private Long idPendiente;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "ID_EVALUACION", nullable = false)
    private Long idEvaluacion;

    @Column(name = "ID_PERIODO")
    private Long idPeriodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoPendiente estado;

    @Lob
    @Column(name = "SNAPSHOT_CONTEXTO")
    private String snapshotContexto;

    @Lob
    @Column(name = "JSON_RESULTADO")
    private String jsonResultado;

    @Lob
    @Column(name = "PROMPT_ENVIADO")
    private String promptEnviado;

    @Column(name = "MODELO_IA_USADO", length = 60)
    private String modeloIaUsado;

    @Column(name = "INTENTOS", nullable = false)
    private Integer intentos;

    @Column(name = "MAX_INTENTOS", nullable = false)
    private Integer maxIntentos;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ULTIMO_INTENTO")
    private LocalDateTime fechaUltimoIntento;

    @Column(name = "FECHA_COMPLETADO")
    private LocalDateTime fechaCompletado;

    @Lob
    @Column(name = "ULTIMO_ERROR")
    private String ultimoError;
}