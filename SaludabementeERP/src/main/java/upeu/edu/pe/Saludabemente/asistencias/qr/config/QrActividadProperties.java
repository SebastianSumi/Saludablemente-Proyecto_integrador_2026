package upeu.edu.pe.Saludabemente.asistencias.qr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.asistencias.util.ConstantesAsistencia;

@Component
@ConfigurationProperties(prefix = "saludablemente.asistencia.qr-actividad")
@Getter
@Setter
public class QrActividadProperties {

    private int rotacionSegundos = ConstantesAsistencia.QR_ROTACION_SEGUNDOS_DEFAULT;
    private int toleranciaRelojSegundos = ConstantesAsistencia.QR_TOLERANCIA_RELOJ_SEGUNDOS_DEFAULT;
    private int ventanaAnticipacionMin = ConstantesAsistencia.QR_VENTANA_ANTICIPACION_MIN_DEFAULT;
    private int ventanaCierreMin = ConstantesAsistencia.QR_VENTANA_CIERRE_MIN_DEFAULT;
    /** Gracia tras rotar: acepta la versión inmediata anterior durante N segundos. */
    private int graciaSegundos = 30;
    private int longitudHash = 32;
}