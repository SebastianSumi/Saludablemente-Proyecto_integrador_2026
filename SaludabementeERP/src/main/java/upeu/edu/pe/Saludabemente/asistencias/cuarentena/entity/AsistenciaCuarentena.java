package upeu.edu.pe.Saludabemente.asistencias.cuarentena.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.asistencias.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ASISTENCIA_CUARENTENA",
        schema = "SLB_ASISTENCIAS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_cuarentena_id_local", columnNames = "id_local")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaCuarentena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuarentena")
    private Long idCuarentena;

    @Column(name = "id_local", nullable = false, length = 36, unique = true)
    private String idLocal;

    @Column(name = "id_actividad", nullable = false)
    private Long idActividad;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asistencia", length = 15)
    private TipoAsistencia tipoAsistencia;

    @Column(name = "hora_marcado")
    private LocalDateTime horaMarcado;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "distancia_geocerca_m", precision = 8, scale = 2)
    private BigDecimal distanciaGeocercaM;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 40)
    private MotivoCuarentena motivo;

    @Column(name = "payload_json", columnDefinition = "text")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoCuarentena estado;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "id_usuario_resuelve")
    private Long idUsuarioResuelve;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoCuarentena.PENDIENTE;
    }
}