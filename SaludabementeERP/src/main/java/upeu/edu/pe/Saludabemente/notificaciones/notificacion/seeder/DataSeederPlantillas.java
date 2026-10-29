package upeu.edu.pe.Saludabemente.notificaciones.notificacion.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.PlantillaRequest;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.PlantillaMensajeRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaService;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

/**
 * Siembra las plantillas de alertas clínicas (canal IN_APP).
 *
 * <p>Idempotente por código de evento: no duplica ni pisa plantillas
 * editadas por UI. Con {@code ddl-auto: create} la tabla nace vacía en
 * cada arranque y este seed la repuebla.</p>
 */
@Component
@Order(110)
@RequiredArgsConstructor
public class DataSeederPlantillas implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederPlantillas.class);

    private final PlantillaMensajeRepository plantillaRepo;
    private final PlantillaService plantillaService;

    @Override
    @Transactional
    public void run(String... args) {
        sembrar("ALERTA_CLINICA_GENERADA", "Nueva alerta clínica",
                "<p>Se generó una alerta clínica de severidad {{severidad}} "
                        + "(puntaje {{puntaje}}, {{indicadores}} indicador(es)).</p>"
                        + "<p>{{mensajeResumen}}</p>");
        sembrar("ALERTA_CLINICA_ATENDIDA", "Alerta clínica atendida",
                "<p>La alerta clínica (severidad {{severidad}}, puntaje {{puntaje}}) "
                        + "fue atendida.</p><p>{{mensajeResumen}}</p>");
        sembrar("ALERTA_CLINICA_ANULADA", "Alerta clínica anulada",
                "<p>La alerta clínica (severidad {{severidad}}, puntaje {{puntaje}}) "
                        + "fue anulada.</p><p>{{mensajeResumen}}</p>");
    }

    private void sembrar(String codigo, String asunto, String cuerpo) {
        if (plantillaRepo.findByCodigoEvento(codigo).isPresent()) {
            log.debug("DataSeederPlantillas: '{}' ya existe. Se omite.", codigo);
            return;
        }
        PlantillaRequest req = new PlantillaRequest();
        req.setCodigoEvento(codigo);
        req.setCanal(CanalNotificacion.IN_APP);
        req.setAsunto(asunto);
        req.setCuerpoHtml(cuerpo);
        req.setActivo(true);
        plantillaService.crear(req);
        log.info("DataSeederPlantillas: plantilla '{}' creada.", codigo);
    }
}
