package upeu.edu.pe.Saludabemente.alertaclinica.colaborador.service;

import java.util.List;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;

public interface MisAlertasService {
    List<AlertaClinicaResponse> listar(EstadoAlerta estado, SeveridadAlerta severidad, TipoEvaluacionOrigen tipoOrigen);
    AlertaClinicaResponse obtener(Long id);
}
