package upeu.edu.pe.Saludabemente.teams.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EQUIPO", schema = "SBL_TEAM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "imagen_key", length = 512)
    private String imagenKey;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "slogan", length = 150)
    private String slogan;

    @Column(name = "referencia_biblica", length = 100)
    private String referenciaBiblica;

    @Column(name = "texto_versiculo", length = 1000)
    private String textoVersiculo;

    @Column(name = "id_usuario_lider")
    private Long idUsuarioLider;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
