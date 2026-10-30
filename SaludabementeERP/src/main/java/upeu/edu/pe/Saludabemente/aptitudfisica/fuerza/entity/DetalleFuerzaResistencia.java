package upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.math.BigDecimal;

/**
 * Detalle de las 3 pruebas de fuerza y resistencia (Excel):
 * abdominales 30s, planchas y salto sin impulso.
 */
@Entity
@Table(name = "FUERZA", schema = "SLB_APTITUD_FISICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleFuerzaResistencia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuerza")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false, unique = true)
    private EvaluacionAptitud evaluacion;

    // ---- Métricas crudas (Excel) ----
    @Column(name = "repeticiones_abdominales", nullable = false)
    private Integer repeticionesAbdominales;

    @Column(name = "repeticiones_flexion_brazo", nullable = false)
    private Integer repeticionesFlexionBrazo;

    @Column(name = "salto_longitud_cm", nullable = false,
            precision = 5, scale = 1)
    private BigDecimal saltoLongitudCm;

    // ---- Diagnósticos calculados por MetricasAptitud ----
    @Column(name = "dx_core", length = 50, nullable = false)
    private String dxCore;

    @Column(name = "dx_tren_superior", length = 50, nullable = false)
    private String dxTrenSuperior;

    @Column(name = "dx_tren_inferior", length = 50, nullable = false)
    private String dxTrenInferior;

    @Column(name = "dx_fuerza_global", length = 50, nullable = false)
    private String dxFuerzaGlobal;
}