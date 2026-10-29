package upeu.edu.pe.Saludabemente.nutricional.antropometrico.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_antropometrico", schema = "SLB_NUTRICIONAL")
@Getter
@Setter
@NoArgsConstructor
public class DetalleAntropometrico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_antropometrico")
    private Long idAntropometrico;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false, unique = true)
    private EvaluacionNutricional evaluacion;

    // ---- Medidas crudas ----
    @Column(name = "estatura_cm", precision = 5, scale = 1)
    private BigDecimal estaturaCm;

    @Column(name = "peso_kg", precision = 5, scale = 1)
    private BigDecimal pesoKg;

    @Column(name = "perimetro_abdominal_cm", precision = 5, scale = 1)
    private BigDecimal perimetroAbdominalCm;

    // ---- Medidas calculadas ----
    @Column(name = "imc", precision = 4, scale = 1)
    private BigDecimal imc;

    @Column(name = "porcentaje_grasa", precision = 4, scale = 1)
    private BigDecimal porcentajeGrasa;

    @Column(name = "porcentaje_masa_muscular", precision = 4, scale = 1)
    private BigDecimal porcentajeMasaMuscular;

    @Column(name = "porcentaje_grasa_visceral", precision = 4, scale = 1)
    private BigDecimal porcentajeGrasaVisceral;

    // ---- Diagnósticos OMRON ----
    @Column(name = "dx_imc", length = 30)
    private String dxImc;

    @Column(name = "dx_perimetro_abdominal", length = 30)
    private String dxPerimetroAbdominal;

    @Column(name = "dx_grasa", length = 30)
    private String dxGrasa;

    @Column(name = "dx_masa_muscular", length = 30)
    private String dxMasaMuscular;

    @Column(name = "dx_grasa_visceral", length = 30)
    private String dxGrasaVisceral;
}