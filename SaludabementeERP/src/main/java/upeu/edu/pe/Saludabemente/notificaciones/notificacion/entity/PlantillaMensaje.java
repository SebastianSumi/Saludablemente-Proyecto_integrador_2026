package upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

@Entity
@Table(name = "PLANTILLA_MENSAJE", schema = "SLB_NOTIFICACION")
@Getter
@Setter
@NoArgsConstructor
public class PlantillaMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANTILLA")
    private Long idPlantilla;

    @Column(name = "CODIGO_EVENTO", nullable = false, unique = true, length = 60)
    private String codigoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "CANAL", nullable = false, length = 20)
    private CanalNotificacion canal;

    @Column(name = "ASUNTO", length = 200)
    private String asunto;

    @Lob
    @Column(name = "CUERPO_HTML", nullable = false)
    private String cuerpoHtml;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo;
}