package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.cardio.entity.DetalleCardiovascular;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.DictamenGlobal;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.fuerza.entity.DetalleFuerzaResistencia;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cabecera transaccional del módulo Aptitud Física.
 * Contiene el snapshot inmutable de la persona al momento de iniciar la prueba.
 * Los diagnósticos se calculan en {@code MetricasAptitud} y se persisten aquí.
 */
@Entity
@Table(name = "EVALUACION", schema = "SLB_APTITUD_FISICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionAptitud extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long id;

    // ---- Hard-IDs (sin FK física — patrón Modulith) ----
    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "id_periodo", nullable = false)
    private Long idPeriodo;

    // ---- Snapshot inmutable (copiado de PersonaSnapshotClinicoDTO) ----
    @Column(name = "documento_identidad_al_momento", length = 15, nullable = false)
    private String documentoIdentidadAlMomento;

    @Column(name = "nombres_completos_al_momento", length = 150, nullable = false)
    private String nombresCompletosAlMomento;

    @Column(name = "edad_al_momento", nullable = false)
    private Integer edadAlMomento;

    @Column(name = "sexo_al_momento", length = 1, nullable = false)
    private String sexoAlMomento;

    @Column(name = "area_trabajo_al_momento", length = 100)
    private String areaTrabajoAlMomento;

    // ---- Ciclo de vida ----
    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_evaluacion", length = 30, nullable = false)
    @Builder.Default
    private EstadoEvaluacion estadoEvaluacion = EstadoEvaluacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dictamen_global", length = 50)
    private DictamenGlobal dictamenGlobal;

    @Column(name = "observaciones_restricciones", length = 500)
    private String observacionesRestricciones;

    @Column(name = "fitness_score", precision = 5, scale = 2)
    private BigDecimal fitnessScore;

    // ---- Relaciones satélite 1:1 (mismo módulo) ----
    @OneToOne(mappedBy = "evaluacion", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private DetalleFuerzaResistencia detalleFuerza;

    @OneToOne(mappedBy = "evaluacion", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private DetalleCardiovascular detalleCardio;
}