package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etiqueta_clinica", schema = "SLB_ALERTAS",
    indexes = {
        @Index(name = "idx_etiqueta_estado", columnList = "estado")
    })
@Getter
@Setter
@NoArgsConstructor
public class EtiquetaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "etiqueta",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<EtiquetaParametro> parametros = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (estado == null) estado = "ACTIVO";
        if (colorHex == null) colorHex = "#2e7d5b";
    }

    @PreUpdate
    void preUpdate() {
        if ("INACTIVO".equals(estado) && fechaBaja == null) {
            fechaBaja = LocalDateTime.now();
        }
    }
}
