package upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

/**
 * Detalle de las 2 pruebas cardiovasculares (Excel):
 * salto de soga 30s y carrera 400m.
 */
@Entity
@Table(name = "CARDIO", schema = "SLB_APTITUD_FISICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCardiovascular extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cardio")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false, unique = true)
    private EvaluacionAptitud evaluacion;

    // ---- Métricas crudas (Excel) ----
    @Column(name = "repeticiones_soga_30s", nullable = false,
            columnDefinition = "NUMBER(3)")
    private Integer repeticionesSoga30s;

    @Column(name = "tiempo_carrera_400m_seg", nullable = false,
            columnDefinition = "NUMBER(4)")
    private Integer tiempoCarrera400mSeg;

    // ---- Diagnóstico calculado por MetricasAptitud ----
    @Column(name = "dx_cardiovascular", length = 50, nullable = false)
    private String dxCardiovascular;
}