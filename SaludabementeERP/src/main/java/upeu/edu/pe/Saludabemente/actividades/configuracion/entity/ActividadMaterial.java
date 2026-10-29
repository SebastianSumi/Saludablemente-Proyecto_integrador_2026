package upeu.edu.pe.Saludabemente.actividades.configuracion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ACTIVIDAD_MATERIAL", schema = "SLB_ACTIVIDADES")
@Getter
@Setter
@NoArgsConstructor
public class ActividadMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MATERIAL")
    private Long idMaterial;

    @Column(name = "ID_ACTIVIDAD", nullable = false)
    private Long idActividad;

    @Column(name = "DESCRIPCION", nullable = false, length = 200)
    private String descripcion;

    public ActividadMaterial(Long idActividad, String descripcion) {
        this.idActividad = idActividad;
        this.descripcion = descripcion;
    }
}