package upeu.edu.pe.Saludabemente.nutricional.bioquimico.entity;

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
@Table(name = "detalle_bioquimico", schema = "SLB_NUTRICIONAL")
@Getter
@Setter
@NoArgsConstructor
public class DetalleBioquimico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bioquimico")
    private Long idBioquimico;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false, unique = true)
    private EvaluacionNutricional evaluacion;

    // ---- Marcadores ----
    @Column(name = "glucosa", precision = 6, scale = 1)
    private BigDecimal glucosa;

    @Column(name = "colesterol", precision = 6, scale = 1)
    private BigDecimal colesterol;

    @Column(name = "trigliceridos", precision = 6, scale = 1)
    private BigDecimal trigliceridos;

    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    // ---- Diagnóstico global ----
    @Column(name = "dx_bioquimico", length = 200)
    private String dxBioquimico;

    // ---- Trazabilidad de importación ----
    @Column(name = "archivo_origen", length = 255)
    private String archivoOrigen;

    @Column(name = "id_usuario_importador")
    private Long idUsuarioImportador;
}