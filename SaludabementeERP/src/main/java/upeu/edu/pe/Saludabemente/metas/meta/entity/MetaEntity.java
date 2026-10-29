package upeu.edu.pe.Saludabemente.metas.meta.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.ModalidadMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.PilarBienestar;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoCalculo;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meta", schema = "SLB_METAS")
@Getter
@Setter
@NoArgsConstructor
public class MetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "id_evaluacion_origen")
    private Long idEvaluacionOrigen;

    @Column(name = "id_alerta_origen")
    private Long idAlertaOrigen;

    @Column(name = "id_profesional_prescriptor")
    private Long idProfesionalPrescriptor;

    @Enumerated(EnumType.STRING)
    @Column(name = "pilar", nullable = false, length = 30)
    private PilarBienestar pilar;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 20)
    private ModalidadMeta modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origen", nullable = false, length = 20)
    private TipoOrigen tipoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 20)
    private TipoCalculo tipoCalculo;

    @Column(name = "indicador", nullable = false, length = 60)
    private String indicador;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "valor_inicial", precision = 10, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_objetivo", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorObjetivo;

    @Column(name = "valor_actual", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorActual = BigDecimal.ZERO;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoMeta estado;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "creado_por", length = 60)
    private String creadoPor;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "modificado_por", length = 60)
    private String modificadoPor;

    @Column(name = "modificado_en")
    private LocalDateTime modificadoEn;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckInEntity> checkIns = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoMeta.ACTIVA;
        }
        if (valorActual == null) {
            valorActual = BigDecimal.ZERO;
        }
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        modificadoEn = LocalDateTime.now();
    }
}