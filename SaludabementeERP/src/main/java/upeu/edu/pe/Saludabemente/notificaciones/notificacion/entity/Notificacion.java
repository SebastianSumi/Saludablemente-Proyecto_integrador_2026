package upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoDelivery;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACION", schema = "SLB_NOTIFICACION")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICACION")
    private Long idNotificacion;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "CANAL", nullable = false, length = 20)
    private CanalNotificacion canal;

    @Column(name = "TITULO", length = 200)
    private String titulo;

    @Column(name = "MENSAJE", columnDefinition = "text")
    private String mensaje;

    @Column(name = "ORIGEN_MODULO", length = 40)
    private String origenModulo;

    @Column(name = "ID_ORIGEN_REFERENCIA")
    private Long idOrigenReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_LECTURA", nullable = false, length = 20)
    private EstadoLectura estadoLectura;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_DELIVERY", nullable = false, length = 20)
    private EstadoDelivery estadoDelivery;

    @Column(name = "FECHA_ENVIO", nullable = false)
    private LocalDateTime fechaEnvio;
}