package upeu.edu.pe.Saludabemente.asistencias.qr.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.qr.dto.response.QrActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrStreamService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QrActividadScheduler {

    private final QrActividadService qrActividadService;
    private final QrStreamService qrStreamService;
    private final ActividadApiService actividadApiService;

    /**
     * Rota el QR de todas las actividades en curso.
     * Se ejecuta cada N segundos según el yml.
     */
    @Scheduled(fixedRateString = "${saludablemente.asistencia.qr-actividad.rotacion-segundos:180}000")
    public void rotarQrActividadesEnCurso() {
        try {
            List<ActividadSnapshotDto> enCurso = actividadApiService.listarEnCurso();
            if (enCurso.isEmpty()) {
                return;
            }
            for (ActividadSnapshotDto a : enCurso) {
                try {
                    QrActividadResponse qr = qrActividadService.rotar(a.idActividad());
                    qrStreamService.emitir(a.idActividad(), qr);
                } catch (Exception e) {
                    log.error("Error rotando QR de actividad {}", a.idActividad(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error en scheduler de rotación QR", e);
        }
    }
}