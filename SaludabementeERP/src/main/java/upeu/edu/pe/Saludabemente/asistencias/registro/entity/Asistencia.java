package upeu.edu.pe.Saludabemente.asistencias.registro.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.asistencias.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ASISTENCIA",
        schema = "SLB_ASISTENCIAS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_asistencia_id_local", columnNames = "id_local"),
                @UniqueConstraint(name = "uq_asistencia_actividad_persona_tipo",
                        columnNames = {"id_actividad", "id_persona", "tipo_asistencia"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long idAsistencia;

    @Column(name = "id_local", nullable = false, length = 36, unique = true)
    private String idLocal;

    @Column(name = "id_actividad", nullable = false)
    private Long idActividad;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asistencia", nullable = false, length = 15)
    private TipoAsistencia tipoAsistencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_registro", nullable = false, length = 25)
    private MetodoRegistro metodoRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "escaneado_por", length = 15)
    private EscaneadoPor escaneadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoAsistencia estado;

    @Column(name = "hora_marcado", nullable = false)
    private LocalDateTime horaMarcado;

    @Column(name = "hora_check_in")
    private LocalDateTime horaCheckIn;

    @Column(name = "hora_check_out")
    private LocalDateTime horaCheckOut;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "distancia_geocerca_m", precision = 8, scale = 2)
    private BigDecimal distanciaGeocercaM;

    @Column(name = "flag_sospechoso", nullable = false)
    private Boolean flagSospechoso;

    @Column(name = "dispositivo_id", length = 60)
    private String dispositivoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 10)
    private SyncStatus syncStatus;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_sync_attempt")
    private LocalDateTime lastSyncAttempt;

    @Column(name = "sync_error", length = 255)
    private String syncError;

    @Column(name = "id_usuario_registro")
    private Long idUsuarioRegistro;

    @Column(name = "id_qr_actividad")
    private Long idQrActividad;

    @Column(name = "codigo_qr_usado", length = 80)
    private String codigoQrUsado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (idLocal == null) {
            idLocal = UUID.randomUUID().toString();
        }
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (flagSospechoso == null) flagSospechoso = false;
        if (retryCount == null) retryCount = 0;
        if (syncStatus == null) syncStatus = SyncStatus.SYNCED;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}