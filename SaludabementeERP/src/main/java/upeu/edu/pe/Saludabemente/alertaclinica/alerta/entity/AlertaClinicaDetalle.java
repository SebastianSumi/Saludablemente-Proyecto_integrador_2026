package upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "alerta_clinica_detalle", schema = "SLB_ALERTAS")
@Getter
@Setter
@NoArgsConstructor
public class AlertaClinicaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_alerta", nullable = false)
    private AlertaClinica alerta;

    @Column(name = "tipo_indicador", nullable = false, length = 40)
    private String tipoIndicador;

    @Column(name = "valor_medido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMedido;

    @Column(name = "valor_min", precision = 10, scale = 2)
    private BigDecimal valorMin;

    @Column(name = "valor_max", precision = 10, scale = 2)
    private BigDecimal valorMax;

    @Column(name = "clasificacion", nullable = false, length = 40)
    private String clasificacion;

    @Column(name = "puntos_riesgo", nullable = false)
    private Integer puntosRiesgo;

    @Column(name = "mensaje", length = 250)
    private String mensaje;

    @Column(name = "id_regla", nullable = false)
    private Long reglaId;

    @Column(name = "entidad_regla", nullable = false, length = 60)
    private String entidadRegla;

    @Column(name = "motor_detonante", nullable = false, length = 30)
    private String motorDetonante;
}