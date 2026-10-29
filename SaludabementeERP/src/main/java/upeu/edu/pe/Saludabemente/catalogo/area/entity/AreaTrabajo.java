package upeu.edu.pe.Saludabemente.catalogo.area.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

@Entity
@Table(name = "CAT_AREA_TRABAJO", schema = "SLB_CATALOGO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaTrabajo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area")
    private Long id;

    @Column(name = "nombre", length = 100, unique = true, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}