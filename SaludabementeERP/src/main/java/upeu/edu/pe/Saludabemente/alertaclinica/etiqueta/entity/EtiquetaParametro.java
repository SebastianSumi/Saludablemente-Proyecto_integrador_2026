package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla puente entre EtiquetaClinica y ParametroEvaluacion.
 *
 * id_etiqueta   -> FK FISICA (mismo agregado: etiqueta).
 * id_parametro  -> FK LOGICA (otro agregado: catalogo de parametros).
 *
 * Los campos parametro_* son FOTOCOPIA de los datos del parametro
 * al momento de asociarlo.
 *
 * PK simple autogenerada + @UniqueConstraint sobre (id_etiqueta, id_parametro)
 * para prevenir duplicados. Mismo patron que DetalleVenta en BomERP S4.
 */
@Entity
@Table(name = "etiqueta_parametro", schema = "SLB_ALERTAS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_etiqueta_parametro",
            columnNames = {"id_etiqueta", "id_parametro"})
    },
    indexes = {
        @Index(name = "idx_etiqueta_parametro_codigo", columnList = "parametro_codigo"),
        @Index(name = "idx_etiqueta_parametro_id", columnList = "id_parametro")
    })
@Getter
@Setter
@NoArgsConstructor
public class EtiquetaParametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etiqueta", nullable = false)
    private EtiquetaClinica etiqueta;

    // FK LOGICA: solo el Long, sin @ManyToOne
    @Column(name = "id_parametro", nullable = false)
    private Long idParametro;

    // FOTOCOPIA del parametro al momento de asociarlo
    @Column(name = "parametro_codigo", nullable = false, length = 40)
    private String parametroCodigo;

    @Column(name = "parametro_nombre", nullable = false, length = 100)
    private String parametroNombre;

    @Column(name = "parametro_unidad", length = 20)
    private String parametroUnidad;
}
