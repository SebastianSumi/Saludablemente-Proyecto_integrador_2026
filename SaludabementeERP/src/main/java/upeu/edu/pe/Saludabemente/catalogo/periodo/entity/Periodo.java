package upeu.edu.pe.Saludabemente.catalogo.periodo.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CAT_PERIODO", schema = "SLB_CATALOGO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Periodo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Long id;

    @Column(name = "codigo", length = 10, unique = true, nullable = false)
    private String codigo;

    @Column(name = "descripcion", length = 100, nullable = false)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoPeriodo estado = EstadoPeriodo.PLANIFICADO;

    // Solo negocio: no es auditoría, es un evento de dominio.
    @Column(name = "cerrado_en")
    private LocalDateTime cerradoEn;

    @Column(name = "cerrado_por", length = 50)
    private String cerradoPor;
}