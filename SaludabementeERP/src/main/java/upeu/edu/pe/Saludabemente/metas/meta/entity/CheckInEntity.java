package upeu.edu.pe.Saludabemente.metas.meta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in", schema = "SLB_METAS")
@Getter
@Setter
@NoArgsConstructor
public class CheckInEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_meta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_checkin_meta"))
    private MetaEntity meta;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "valor_reportado", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorReportado;

    @Column(name = "logrado")
    private Boolean logrado;

    @Column(name = "nota", length = 300)
    private String nota;

    @Column(name = "evidencia_url", length = 300)
    private String evidenciaUrl;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}