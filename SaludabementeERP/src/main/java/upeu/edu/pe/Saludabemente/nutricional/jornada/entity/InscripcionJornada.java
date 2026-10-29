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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.nutricional.jornada.enums.EstadoInscripcion;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

import java.time.LocalDateTime;

/**
 * Inscripción de una persona a una jornada de evaluación nutricional.
 *
 * <p>La persona se identifica con {@code idPersona}, el identificador
 * estable que Seguridad mantiene en {@code Usuario.idPersonaSnapshot}.
 * No se crea una FK física hacia el módulo Personal.</p>
 *
 * <p>Las restricciones únicas {@code (id_jornada, id_persona)} y
 * {@code (id_jornada, numero_turno)} son la segunda línea de defensa
 * ante condiciones de carrera en la asignación de turnos.</p>
 */
@Entity
@Table(
        name = "inscripcion_jornada",
        schema = "SLB_NUTRICIONAL",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_INSCRIPCION_JORNADA_PERSONA",
                        columnNames = {"id_jornada", "id_persona"}),
                @UniqueConstraint(
                        name = "UK_INSCRIPCION_JORNADA_TURNO",
                        columnNames = {"id_jornada", "numero_turno"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionJornada extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Long idInscripcion;

    @Column(name = "id_jornada", nullable = false)
    private Long idJornada;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "documento_identidad", length = 15)
    private String documentoIdentidad;

    @Column(name = "nombres_completos", length = 200)
    private String nombresCompletos;

    @Column(name = "numero_turno", nullable = false)
    private Integer numeroTurno;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoInscripcion estado = EstadoInscripcion.ACTIVA;

    @PrePersist
    void inicializarDefaults() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoInscripcion.ACTIVA;
        }
    }
}
