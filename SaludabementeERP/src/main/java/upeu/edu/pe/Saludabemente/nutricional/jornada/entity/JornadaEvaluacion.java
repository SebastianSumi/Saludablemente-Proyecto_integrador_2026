package upeu.edu.pe.Saludabemente.nutricional.jornada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoJornada;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Jornada de evaluación nutricional.
 *
 * <p>Es una entidad independiente de {@code Actividad} y de
 * {@code EvaluacionNutricional}: solo organiza citas/turnos para futuras
 * evaluaciones. No registra resultados clínicos.</p>
 *
 * <p>El periodo se referencia por ID lógico (hard-id) hacia
 * {@code SLB_CATALOGO.CAT_PERIODO}, siguiendo el patrón de módulos
 * desacoplados del proyecto.</p>
 */
@Entity
@Table(name = "jornada_evaluacion", schema = "SLB_NUTRICIONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JornadaEvaluacion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jornada")
    private Long idJornada;

    @Column(name = "id_periodo", nullable = false)
    private Long idPeriodo;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoJornada estado = EstadoJornada.ABIERTA;

    @PrePersist
    void inicializarDefaults() {
        if (estado == null) {
            estado = EstadoJornada.ABIERTA;
        }
    }
}
