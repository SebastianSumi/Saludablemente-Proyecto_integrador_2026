package upeu.edu.pe.Saludabemente.actividades.imagen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ACTIVIDAD_IMAGEN", schema = "SLB_ACTIVIDADES")
@Getter
@Setter
@NoArgsConstructor
public class ActividadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_IMAGEN")
    private Long idImagen;

    @Column(name = "ID_ACTIVIDAD", nullable = false)
    private Long idActividad;

    @Column(name = "IMAGEN_KEY", nullable = false, length = 255)
    private String imagenKey;

    @Column(name = "IMAGEN_URL", nullable = false, length = 500)
    private String imagenUrl;

    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @Column(name = "ORDEN")
    private Integer orden;

    @Column(name = "FECHA_SUBIDA", nullable = false)
    private LocalDateTime fechaSubida;

    public ActividadImagen(Long idActividad, String imagenKey, String imagenUrl,
                           String descripcion, Integer orden) {
        this.idActividad = idActividad;
        this.imagenKey = imagenKey;
        this.imagenUrl = imagenUrl;
        this.descripcion = descripcion;
        this.orden = orden;
        this.fechaSubida = LocalDateTime.now();
    }
}