package upeu.edu.pe.Saludabemente.alertaclinica.api;

import upeu.edu.pe.Saludabemente.alertaclinica.api.dto.AlertaReporteDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaReporteApiService {
    List<AlertaReporteDTO> listarPorPersona(Long idPersona, LocalDateTime desde, LocalDateTime hasta);
}
