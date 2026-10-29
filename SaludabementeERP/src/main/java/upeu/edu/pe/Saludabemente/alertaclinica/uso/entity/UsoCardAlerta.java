package upeu.edu.pe.Saludabemente.alertaclinica.uso.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Frecuencia de uso de un card del menú de alertas clínicas, por usuario.
 * Permite reordenar dinámicamente las tarjetas según la preferencia real.
 */
@Entity
@Table(
        name = "uso_card_alerta",
        schema = "SLB_ALERTAS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_uso_card_usuario_ruta",
                columnNames = {"id_usuario", "ruta"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UsoCardAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "ruta", nullable = false, length = 160)
    private String ruta;

    @Column(name = "conteo", nullable = false)
    private Integer conteo;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        if (conteo == null) {
            conteo = 0;
        }
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
