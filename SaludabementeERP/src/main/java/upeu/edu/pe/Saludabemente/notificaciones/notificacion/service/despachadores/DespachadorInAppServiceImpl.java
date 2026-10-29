package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaEngineService;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoDelivery;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DespachadorInAppServiceImpl implements DespachadorInAppService {

    private final NotificacionRepository notificacionRepository;
    private final PlantillaEngineService plantillaEngine;

    @Override
    @Transactional
    public Notificacion despachar(Long idPersona,
                                  String codigoEvento,
                                  String origenModulo,
                                  Long idOrigenReferencia,
                                  Map<String, Object> variables) {

        String asunto = plantillaEngine.renderizarAsunto(codigoEvento, CanalNotificacion.IN_APP, variables);
        String cuerpo = plantillaEngine.renderizarCuerpo(codigoEvento, CanalNotificacion.IN_APP, variables);

        Notificacion notificacion = new Notificacion();
        notificacion.setIdPersona(idPersona);
        notificacion.setCanal(CanalNotificacion.IN_APP);
        notificacion.setTitulo(asunto);
        notificacion.setMensaje(cuerpo);
        notificacion.setOrigenModulo(origenModulo);
        notificacion.setIdOrigenReferencia(idOrigenReferencia);
        notificacion.setEstadoLectura(EstadoLectura.NO_LEIDA);
        notificacion.setEstadoDelivery(EstadoDelivery.ENVIADO);
        notificacion.setFechaEnvio(LocalDateTime.now());

        return notificacionRepository.save(notificacion);
    }
}