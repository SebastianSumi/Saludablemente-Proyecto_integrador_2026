package upeu.edu.pe.Saludabemente.alertaclinica.colaborador.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MisAlertasServiceImpl implements MisAlertasService {
    private final SeguridadService seguridad;
    private final AlertaClinicaRepository repository;
    private final AlertaClinicaMapper mapper;

    private Long miPersona() {
        var perfil = seguridad.obtenerMiPerfil();
        if (!Boolean.TRUE.equals(perfil.activo()) || perfil.idPersona() == null || perfil.idPersona() <= 0)
            throw new AccessDeniedException("Tu usuario no tiene una persona activa vinculada");
        return perfil.idPersona();
    }

    @Override
    public List<AlertaClinicaResponse> listar(EstadoAlerta estado, SeveridadAlerta severidad, TipoEvaluacionOrigen tipoOrigen) {
        return mapper.toResponseList(repository.buscar(estado, severidad, miPersona(), tipoOrigen, null, null,
                Sort.by(Sort.Direction.DESC, "fechaGeneracion", "idAlerta")));
    }

    @Override
    public AlertaClinicaResponse obtener(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Identificador de alerta invalido");
        // Filtra por propietario en SQL antes de mapear o devolver datos. No revela si una alerta ajena existe.
        return mapper.toResponse(repository.findByIdAlertaAndPersonaId(id, miPersona())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada")));
    }
}
