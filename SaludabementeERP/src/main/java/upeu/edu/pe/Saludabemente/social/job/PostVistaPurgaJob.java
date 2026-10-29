package upeu.edu.pe.Saludabemente.social.job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.social.post.repository.PostVistaRepository;
import java.time.*;
@Component @RequiredArgsConstructor
public class PostVistaPurgaJob {
    private final PostVistaRepository vistas;
    private final Clock socialClock;
    @org.springframework.beans.factory.annotation.Value("${saludablemente.social.alcance.purga-dias:180}")
    private int purgaDias=180;
    @Scheduled(cron="0 0 3 1 * *",zone="America/Lima") @Transactional
    public void purgar() { if(purgaDias>0) vistas.deleteByFechaUltimaVistaBefore(LocalDateTime.now(socialClock).minusDays(purgaDias)); }
}
