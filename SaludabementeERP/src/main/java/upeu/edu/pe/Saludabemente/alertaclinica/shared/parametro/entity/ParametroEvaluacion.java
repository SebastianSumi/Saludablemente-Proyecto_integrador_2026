package upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parametro_evaluacion", schema = "SLB_ALERTAS",
    indexes = {
        // NOTA: idx_parametro_codigo fue eliminado porque @Column(unique=true) en Oracle
        // ya genera un indice implicito para la constraint UNIQUE. Mantenerlo causaba ORA-01408.
        @Index(name = "idx_parametro_estado", columnList = "estado")
    })
@Getter
@Setter
@NoArgsConstructor
public class ParametroEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @PrePersist
    void prePersist() {
        if (estado == null) estado = "ACTIVO";
    }

    @PreUpdate
    void preUpdate() {
        if ("INACTIVO".equals(estado) && fechaBaja == null) {
            fechaBaja = LocalDateTime.now();
        }
    }
}
