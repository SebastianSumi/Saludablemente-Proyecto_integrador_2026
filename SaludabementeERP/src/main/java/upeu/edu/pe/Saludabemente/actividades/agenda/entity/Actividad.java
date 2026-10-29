package upeu.edu.pe.Saludabemente.actividades.agenda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Entity
@Table(name = "ACTIVIDAD", schema = "SLB_ACTIVIDADES")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACTIVIDAD")
    private Long idActividad;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 300)
    private String descripcion;

    @Column(name = "FECHA_ACTIVIDAD", nullable = false)
    private LocalDate fechaActividad;

    @Column(name = "HORA_INICIO", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "HORA_FIN", nullable = false)
    private LocalTime horaFin;

    @Column(name = "LUGAR", nullable = false, length = 100)
    private String lugar;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;   // PROGRAMADA / EN_CURSO / FINALIZADA / CANCELADA

    @Column(name = "ID_USUARIO_CREADOR", nullable = false)
    private Long idUsuarioCreador;

    /** Atribución opcional explícita; no se infiere de creador/coordinador. */
    @Column(name = "ID_EQUIPO_ACTIVIDAD")
    private Long idEquipoActividad;

    @Column(name = "FECHA_CREACION")
    private LocalDateTime fechaCreacion;

    @Column(name = "ID_USUARIO_COORDINADOR", nullable = false)
    private Long idUsuarioCoordinador;

    @Column(name = "PUNTAJE_SALUD")
    private Integer puntajeSalud;

    @Column(name = "LOGO_KEY", length = 255)
    private String logoKey;        // "actividades/1/logo/abc123.png"

    @Column(name = "LOGO_URL", length = 500)
    private String logoUrl;        // "http://localhost:9000/saludablemente/actividades/1/logo/abc123.png"

    // ============================================================
    // Campos para Asistencias (FASE A)
    // ============================================================

    @Column(name = "LATITUD_OFICIAL", precision = 10, scale = 8)
    private BigDecimal latitudOficial;

    @Column(name = "LONGITUD_OFICIAL", precision = 11, scale = 8)
    private BigDecimal longitudOficial;

    @Column(name = "TOLERANCIA_GEOCERCA_METROS")
    private Integer toleranciaGeocercaMetros;

    @Column(name = "REQUIERE_CHECK_IN_OUT")
    private Boolean requiereCheckInOut;

    @Column(name = "DURACION_MINUTOS")
    private Integer duracionMinutos;

    @Column(name = "PERIODO", length = 10)
    private String periodo;

    @Column(name = "PERMITE_AUTOSERVICIO")
    private Boolean permiteAutoservicio;

    @Column(name = "PERMITE_ESCANEO_COORDINADOR")
    private Boolean permiteEscaneoCoordinador;

    @Column(name = "CIERRE_ASISTENCIAS_REALIZADO")
    private Boolean cierreAsistenciasRealizado;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
