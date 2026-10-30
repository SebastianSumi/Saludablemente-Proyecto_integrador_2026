package upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACION_DLQ", schema = "SLB_NOTIFICACION")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionDlq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DLQ")
    private Long idDlq;

    @Column(name = "ID_NOTIFICACION")
    private Long idNotificacion;

    @Column(name = "DESTINATARIO", length = 120)
    private String destinatario;

    @Column(name = "CODIGO_EVENTO", length = 60)
    private String codigoEvento;

    @Column(name = "PAYLOAD", columnDefinition = "text")
    private String payload;

    @Column(name = "ERROR", length = 500)
    private String error;

    @Column(name = "INTENTOS", nullable = false)
    private Integer intentos;

    @Column(name = "PROXIMO_INTENTO")
    private LocalDateTime proximoIntento;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "RESUELTO", nullable = false)
    private Boolean resuelto;
}