package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;

import java.time.LocalDateTime;

/**
 * Tabla puente entre AlertaClinica y EtiquetaClinica.
 *
 * id_alerta    -> FK FISICA (mismo agregado: alerta).
 * id_etiqueta  -> FK LOGICA (otro agregado: catalogo de etiquetas).
 *
 * Los campos etiqueta_* son FOTOCOPIA de los datos de la etiqueta
 * al momento de asignarla.
 *
 * PK simple autogenerada + @UniqueConstraint sobre (id_alerta, id_etiqueta).
 * Mismo patron que DetalleVenta en BomERP S4.
 */
@Entity
@Table(name = "alerta_etiqueta", schema = "SLB_ALERTAS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_alerta_etiqueta",
            columnNames = {"id_alerta", "id_etiqueta"})
    },
    indexes = {
        @Index(name = "idx_alerta_etiqueta_alerta", columnList = "id_alerta"),
        @Index(name = "idx_alerta_etiqueta_etiqueta", columnList = "id_etiqueta"),
        @Index(name = "idx_alerta_etiqueta_codigo", columnList = "etiqueta_codigo")
    })
@Getter
@Setter
@NoArgsConstructor
public class AlertaEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alerta", nullable = false)
    private AlertaClinica alerta;

    // FK LOGICA: solo el Long, sin @ManyToOne
    @Column(name = "id_etiqueta", nullable = false)
    private Long idEtiqueta;

    // FOTOCOPIA de la etiqueta al momento de asignarla
    @Column(name = "etiqueta_codigo", nullable = false, length = 40)
    private String etiquetaCodigo;

    @Column(name = "etiqueta_nombre", nullable = false, length = 100)
    private String etiquetaNombre;

    @Column(name = "etiqueta_color_hex", nullable = false, length = 7)
    private String etiquetaColorHex;

    @Column(name = "asignada_por", nullable = false, length = 20)
    private String asignadaPor;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    void prePersist() {
        if (asignadaPor == null) asignadaPor = "MOTOR";
        if (fechaAsignacion == null) fechaAsignacion = LocalDateTime.now();
    }
}
