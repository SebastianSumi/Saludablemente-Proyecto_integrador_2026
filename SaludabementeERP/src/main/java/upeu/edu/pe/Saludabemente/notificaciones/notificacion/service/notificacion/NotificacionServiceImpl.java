package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.notificacion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.BandejaNotificacionDTO;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.dto.NotificacionResponse;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.mapper.NotificacionMapper;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionRepository;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.EstadoLectura;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionMapper notificacionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar() {
        return notificacionRepository.findAll()
                .stream()
                .map(notificacionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Long id) {
        return notificacionMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BandejaNotificacionDTO> listarBandeja(Long idPersona) {
        return notificacionRepository
                .findByIdPersonaAndEstadoLecturaNotOrderByFechaEnvioDesc(idPersona, EstadoLectura.ARCHIVADA)
                .stream()
                .map(notificacionMapper::toBandeja)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(Long idPersona) {
        return notificacionRepository.countByIdPersonaAndEstadoLectura(idPersona, EstadoLectura.NO_LEIDA);
    }

    @Override
    @Transactional
    public void marcarLeida(Long idNotificacion) {
        Notificacion notificacion = buscarOFallar(idNotificacion);
        notificacion.setEstadoLectura(EstadoLectura.LEIDA);
        notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional
    public void archivar(Long idNotificacion) {
        Notificacion notificacion = buscarOFallar(idNotificacion);
        notificacion.setEstadoLectura(EstadoLectura.ARCHIVADA);
        notificacionRepository.save(notificacion);
    }

    private Notificacion buscarOFallar(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación no encontrada: " + id));
    }
}