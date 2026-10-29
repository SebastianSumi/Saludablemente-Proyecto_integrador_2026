package upeu.edu.pe.Saludabemente.asistencias.exoneracion.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;

import java.time.LocalDateTime;

@Entity
@Table(name = "JUSTIFICACION", schema = "SLB_ASISTENCIAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Justificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justificacion")
    private Long idJustificacion;

    @Column(name = "id_actividad", nullable = false)
    private Long idActividad;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Column(name = "url_documento", length = 250)
    private String urlDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoJustificacion estado;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "id_usuario_dictamina")
    private Long idUsuarioDictamina;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_dictamen")
    private LocalDateTime fechaDictamen;

    @PrePersist
    public void prePersist() {
        if (fechaSolicitud == null) fechaSolicitud = LocalDateTime.now();
        if (estado == null) estado = EstadoJustificacion.PENDIENTE;
    }
}