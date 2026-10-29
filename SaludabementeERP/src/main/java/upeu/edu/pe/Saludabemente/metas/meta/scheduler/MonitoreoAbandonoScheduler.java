package upeu.edu.pe.Saludabemente.metas.meta.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.NotificacionMetasService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoreoAbandonoScheduler {

    private static final int DIAS_ABANDONO = 7;

    private final MetaRepository metaRepository;
    private final NotificacionMetasService notificacionMetas;
    private final AuditoriaMetasService auditoriaMetas;

    // =================================================================
    // BARRIDO 1: metas clinicas sin check-in en 7 dias -> DESVIADA
    // =================================================================
    @Scheduled(cron = "0 0 2 * * ?", zone = "America/Lima")
    @Transactional
    public void detectarAbandonos() {

        LocalDate desde = LocalDate.now().minusDays(DIAS_ABANDONO);
        List<MetaEntity> candidatas = metaRepository.findClinicasSinCheckInDesde(
                EstadoMeta.ACTIVA, TipoOrigen.CLINICA, desde);

        if (candidatas.isEmpty()) {
            log.debug("[Scheduler metas] Sin abandonos detectados");
            return;
        }

        for (MetaEntity meta : candidatas) {
            meta.setEstado(EstadoMeta.DESVIADA);
            meta.setModificadoPor("scheduler");
        }
        List<MetaEntity> guardadas = metaRepository.saveAll(candidatas);

        log.warn("[Scheduler metas] {} metas marcadas DESVIADA", guardadas.size());

        // Registrar efectos secundarios para despues del commit
        registrarPostCommit(() -> {
            for (MetaEntity meta : guardadas) {
                try {
                    auditoriaMetas.registrarCambioPorScheduler(
                            meta.getId(),
                            EstadoMeta.ACTIVA.name(),
                            EstadoMeta.DESVIADA.name(),
                            DIAS_ABANDONO + " dias sin check-in");

                    notificacionMetas.notificarMetaDesviada(
                            meta.getIdPersona(),
                            meta.getId(),
                            meta.getDescripcion());

                    if (meta.getIdProfesionalPrescriptor() != null) {
                        notificacionMetas.notificarEvaluador(
                                meta.getIdProfesionalPrescriptor(),
                                meta.getId(),
                                "Meta clinica desviada por inactividad");
                    }
                } catch (RuntimeException ex) {
                    log.warn("[Scheduler metas] Fallo al procesar meta {}: {}",
                            meta.getId(), ex.getMessage());
                }
            }
        });
    }

    // =================================================================
    // BARRIDO 2: metas activas con fecha objetivo vencida -> ABANDONADA
    // =================================================================
    @Scheduled(cron = "0 30 2 * * ?", zone = "America/Lima")
    @Transactional
    public void vencerMetasExpiradas() {

        List<MetaEntity> candidatas = metaRepository.findActivasVencidas(
                EstadoMeta.ACTIVA, LocalDate.now());

        if (candidatas.isEmpty()) {
            log.debug("[Scheduler metas] Sin metas vencidas");
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (MetaEntity meta : candidatas) {
            meta.setEstado(EstadoMeta.ABANDONADA);
            meta.setModificadoPor("scheduler");
            meta.setFechaCierre(ahora);
        }
        List<MetaEntity> guardadas = metaRepository.saveAll(candidatas);

        log.warn("[Scheduler metas] {} metas marcadas ABANDONADA", guardadas.size());

        registrarPostCommit(() -> {
            for (MetaEntity meta : guardadas) {
                try {
                    auditoriaMetas.registrarCambioPorScheduler(
                            meta.getId(),
                            EstadoMeta.ACTIVA.name(),
                            EstadoMeta.ABANDONADA.name(),
                            "Fecha objetivo vencida sin completarse");
                } catch (RuntimeException ex) {
                    log.warn("[Scheduler metas] Fallo al auditar meta vencida {}: {}",
                            meta.getId(), ex.getMessage());
                }
            }
        });
    }

    // =================================================================
    // HELPER: registrar efecto post-commit
    // =================================================================
    private void registrarPostCommit(Runnable efecto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            efecto.run();
                        }
                    });
        } else {
            // Fallback: no hay transaccion activa, ejecutar directo
            efecto.run();
        }
    }



}