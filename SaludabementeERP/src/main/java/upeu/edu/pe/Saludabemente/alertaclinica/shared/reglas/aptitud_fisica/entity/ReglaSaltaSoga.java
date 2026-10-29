package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.ClasificacionPruebaFisica;

import java.time.LocalDateTime;

@Entity
@Table(name = "regla_salta_soga", schema = "SLB_ALERTAS")
@Getter
@Setter
@NoArgsConstructor
public class ReglaSaltaSoga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idReglaSaltaSoga;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 1)
    private SexoBiologico sexo;

    @Column(name = "edad_min", nullable = false)
    private Integer edadMin;

    @Column(name = "edad_max", nullable = false)
    private Integer edadMax;

    @Column(name = "saltos_min", nullable = false)
    private Integer saltosMin;

    @Column(name = "saltos_max", nullable = false)
    private Integer saltosMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "direccion_riesgo", nullable = false, length = 15)
    private DireccionRiesgo direccionRiesgo;

    @Column(name = "puntos_riesgo", nullable = false)
    private Integer puntosRiesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 30)
    private ClasificacionPruebaFisica clasificacion;

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