package upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.entity.DetalleAntropometrico;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.entity.DetalleBioquimico;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "evaluacion_nutricional", schema = "SLB_NUTRICIONAL")
@Getter
@Setter
@NoArgsConstructor
public class EvaluacionNutricional extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long idEvaluacion;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "id_periodo", nullable = false, length = 10)
    private String idPeriodo;

    // ---- Snapshot clínico congelado al iniciar ----
    @Column(name = "documento_identidad", length = 8, nullable = false)
    private String documentoIdentidad;

    @Column(name = "nombres_completos", length = 200)
    private String nombresCompletos;

    @Column(name = "edad_al_momento")
    private Integer edadAlMomento;

    @Column(name = "sexo_al_momento", length = 1)
    private String sexoAlMomento;

    @Column(name = "area_trabajo_al_momento", length = 100)
    private String areaTrabajoAlMomento;

    // ---- Ciclo de vida ----
    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_evaluacion", nullable = false, length = 20)
    private EstadoEvaluacion estadoEvaluacion;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // ---- Detalles (FK vive en los detalles) ----
    @OneToOne(mappedBy = "evaluacion", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private DetalleAntropometrico detalleAntropometrico;

    @OneToOne(mappedBy = "evaluacion", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private DetalleBioquimico detalleBioquimico;

    // ---- Helpers de consistencia bidireccional ----
    public void asignarAntropometrico(DetalleAntropometrico detalle) {
        if (detalle != null) {
            detalle.setEvaluacion(this);
        }
        this.detalleAntropometrico = detalle;
    }

    public void asignarBioquimico(DetalleBioquimico detalle) {
        if (detalle != null) {
            detalle.setEvaluacion(this);
        }
        this.detalleBioquimico = detalle;
    }

    @PrePersist
    void inicializarDefaults() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = LocalDate.now();
        }
        if (estadoEvaluacion == null) {
            estadoEvaluacion = EstadoEvaluacion.PENDIENTE;
        }
    }
}