package upeu.edu.pe.Saludabemente.alertaclinica.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.AlertaClinicaDetalle;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.api.AlertasMetasApiService;
import upeu.edu.pe.Saludabemente.alertaclinica.api.dto.ResumenAlertaDTO;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertasMetasApiServiceImpl implements AlertasMetasApiService {

    private final AlertaClinicaRepository alertaRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existeAlertaActiva(Long idAlerta, Long idPersona) {
        if (idAlerta == null) {
            throw new IllegalArgumentException("idAlerta no puede ser null");
        }
        if (idPersona == null) {
            throw new IllegalArgumentException("idPersona no puede ser null");
        }

        return alertaRepository.existsByIdAlertaAndPersonaIdAndEstado(
                idAlerta, idPersona, EstadoAlerta.PENDIENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenAlertaDTO obtenerResumen(Long idAlerta) {
        if (idAlerta == null) {
            throw new IllegalArgumentException("idAlerta no puede ser null");
        }

        AlertaClinica alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada: " + idAlerta));

        List<String> indicadores = alerta.getDetalles().stream()
                .map(AlertaClinicaDetalle::getTipoIndicador)
                .distinct()
                .toList();

        return new ResumenAlertaDTO(
                alerta.getIdAlerta(),
                alerta.getPersonaId(),
                alerta.getEvaluacionOrigenId(),
                indicadores,
                alerta.getNivelSeveridad().name(),
                alerta.getEstado().name(),
                alerta.getFechaGeneracion()
        );
    }
}