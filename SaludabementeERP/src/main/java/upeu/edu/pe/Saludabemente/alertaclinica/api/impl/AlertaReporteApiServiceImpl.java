package upeu.edu.pe.Saludabemente.alertaclinica.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.api.AlertaReporteApiService;
import upeu.edu.pe.Saludabemente.alertaclinica.api.dto.AlertaReporteDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaReporteApiServiceImpl implements AlertaReporteApiService {

    private final AlertaClinicaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaReporteDTO> listarPorPersona(Long idPersona, LocalDateTime desde, LocalDateTime hasta) {
        if (idPersona == null) return List.of();
        List<AlertaClinica> alertas = repository.buscar(null, null, idPersona, null, desde, hasta,
                Sort.by(Sort.Direction.DESC, "fechaGeneracion"));
        return alertas.stream().map(a -> new AlertaReporteDTO(
                a.getIdAlerta(),
                a.getPersonaId(),
                a.getTipoEvaluacionOrigen() == null ? null : a.getTipoEvaluacionOrigen().name(),
                a.getPuntajeTotal(),
                a.getNivelSeveridad() == null ? null : a.getNivelSeveridad().name(),
                a.getEstado() == null ? null : a.getEstado().name(),
                a.getFechaGeneracion(),
                a.getDetalles() == null ? 0 : a.getDetalles().size()
        )).toList();
    }
}
