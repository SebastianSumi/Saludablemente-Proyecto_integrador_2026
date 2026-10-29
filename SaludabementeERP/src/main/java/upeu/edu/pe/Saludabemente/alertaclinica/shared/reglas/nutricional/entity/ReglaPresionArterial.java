package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPresionArterial;

import java.time.LocalDateTime;

@Entity
@Table(name = "regla_presion_arterial", schema = "SLB_ALERTAS")
@Getter
@Setter
@NoArgsConstructor
public class ReglaPresionArterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idReglaPresion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 1)
    private SexoBiologico sexo;

    @Column(name = "edad_min", nullable = false)
    private Integer edadMin;

    @Column(name = "edad_max", nullable = false)
    private Integer edadMax;

    @Column(name = "sistolica_mm_hg_min", nullable = false)
    private Integer sistolicaMmHgMin;

    @Column(name = "sistolica_mm_hg_max", nullable = false)
    private Integer sistolicaMmHgMax;

    @Column(name = "diastolica_mm_hg_min", nullable = false)
    private Integer diastolicaMmHgMin;

    @Column(name = "diastolica_mm_hg_max", nullable = false)
    private Integer diastolicaMmHgMax;

    @Column(name = "puntos_riesgo", nullable = false)
    private Integer puntosRiesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 30)
    private ClasificacionPresionArterial clasificacion;

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