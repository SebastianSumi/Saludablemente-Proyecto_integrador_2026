package upeu.edu.pe.Saludabemente.asistencias.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.registro.service.AsistenciaService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CierreAsistenciasScheduler {

    private final ActividadApiService actividadApiService;
    private final AsistenciaService asistenciaService;

    /**
     * Cada hora, busca actividades FINALIZADAS sin cierre realizado
     * y ejecuta el cierre de asistencias.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cerrarActividadesFinalizadas() {
        try {
            List<ActividadSnapshotDto> pendientes = actividadApiService.listarFinalizadasNoCerradas();
            if (pendientes.isEmpty()) {
                return;
            }
            log.info("Cierre automático: {} actividades pendientes", pendientes.size());

            for (ActividadSnapshotDto a : pendientes) {
                try {
                    asistenciaService.generarCierreDeAsistencias(a.idActividad());
                } catch (Exception e) {
                    log.error("Error cerrando actividad {}", a.idActividad(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error en scheduler de cierre", e);
        }
    }
}