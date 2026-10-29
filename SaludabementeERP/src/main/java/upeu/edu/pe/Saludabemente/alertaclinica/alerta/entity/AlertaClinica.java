package upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.AlertaEtiqueta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "alerta_clinica", schema = "SLB_ALERTAS")
@Getter
@Setter
@NoArgsConstructor
public class AlertaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idAlerta;

    @Column(name = "id_persona", nullable = false)
    private Long personaId;

    @Column(name = "id_evaluacion_origen", nullable = false)
    private Long evaluacionOrigenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evaluacion_origen", nullable = false, length = 20)
    private TipoEvaluacionOrigen tipoEvaluacionOrigen;

    @Column(name = "puntaje_total", nullable = false)
    private Integer puntajeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidad", nullable = false, length = 15)
    private SeveridadAlerta nivelSeveridad;

    @Column(name = "mensaje_resumen", length = 300)
    private String mensajeResumen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoAlerta estado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Column(name = "id_usuario_atiende")
    private Long usuarioAtiendeId;

    @Column(name = "observacion_atencion", length = 300)
    private String observacionAtencion;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlertaClinicaDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "alerta",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private Set<AlertaEtiqueta> etiquetas = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoAlerta.PENDIENTE;
        }
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
    }
}