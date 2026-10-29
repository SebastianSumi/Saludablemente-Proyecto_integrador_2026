package upeu.edu.pe.Saludabemente.asistencias.qr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "QR_ACTIVIDAD",
        schema = "SLB_ASISTENCIAS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_qr_actividad_codigo", columnNames = "codigo_qr"),
                @UniqueConstraint(name = "uq_qr_actividad_version", columnNames = {"id_actividad", "version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_qr_actividad")
    private Long idQrActividad;

    @Column(name = "id_actividad", nullable = false)
    private Long idActividad;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "codigo_qr", nullable = false, length = 80)
    private String codigoQr;

    @Column(name = "ventana_inicio", nullable = false)
    private LocalDateTime ventanaInicio;

    @Column(name = "ventana_fin", nullable = false)
    private LocalDateTime ventanaFin;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "generado_en", nullable = false, updatable = false)
    private LocalDateTime generadoEn;

    @Column(name = "generado_por")
    private Long generadoPor;

    @PrePersist
    public void prePersist() {
        if (generadoEn == null) generadoEn = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}