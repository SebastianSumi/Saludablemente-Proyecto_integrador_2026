package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionColesterol;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "regla_colesterol", schema = "SLB_ALERTAS")
@Getter
@Setter
@NoArgsConstructor
public class ReglaColesterol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idReglaColesterol;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 1)
    private SexoBiologico sexo;

    @Column(name = "edad_min", nullable = false)
    private Integer edadMin;

    @Column(name = "edad_max", nullable = false)
    private Integer edadMax;

    @Column(name = "colesterol_mg_dl_min", nullable = false, precision = 6, scale = 2)
    private BigDecimal colesterolMgDlMin;

    @Column(name = "colesterol_mg_dl_max", nullable = false, precision = 6, scale = 2)
    private BigDecimal colesterolMgDlMax;

    @Column(name = "puntos_riesgo", nullable = false)
    private Integer puntosRiesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 30)
    private ClasificacionColesterol clasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoRegla estado;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoRegla.ACTIVO;
        }
    }

    @PreUpdate
    void preUpdate() {
        if (EstadoRegla.INACTIVO.equals(estado) && fechaBaja == null) {
            fechaBaja = LocalDateTime.now();
        }
    }
}