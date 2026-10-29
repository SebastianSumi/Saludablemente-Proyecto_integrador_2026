package upeu.edu.pe.Saludabemente.noticias.categoria.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.shared.entity.AuditableEntity;

@Entity
@Table(name = "CATEGORIA", schema = "SLB_NOTICIAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORIA")
    private Long id;

    @Column(name = "NOMBRE", length = 60, nullable = false, unique = true)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @Column(name = "COLOR_HEX", length = 7)
    private String colorHex;

    @Column(name = "ICONO", length = 60)
    private String icono;

    @Column(name = "ORDEN", nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "ES_SISTEMA", nullable = false)
    @Builder.Default
    private Boolean esSistema = false;

    @Column(name = "ACTIVO", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}