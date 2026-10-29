package upeu.edu.pe.Saludabemente.alertaclinica.alerta.service;

import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaReporte;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AtenderAlertaRequest;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAgregadoDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaClinicaService {

    List<AlertaClinicaResponse> buscar(
            EstadoAlerta estado,
            SeveridadAlerta severidad,
            Long personaId,
            TipoEvaluacionOrigen tipoOrigen,
            LocalDateTime desde,
            LocalDateTime hasta,
            List<String> codigosEtiquetas,
            String ordenarPor,
            String direccion);

    org.springframework.data.domain.Page<upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen> buscarOpciones(
            Long personaId, EstadoAlerta estado, SeveridadAlerta severidad, LocalDateTime desde,
            LocalDateTime hasta, int pagina, int tamanio);

    AlertaClinicaResponse obtener(Long id);

    AlertaClinicaResponse atender(Long id, AtenderAlertaRequest request);

    AlertaClinicaResponse anular(Long id);

    AlertaReporte reporte(LocalDateTime desde, LocalDateTime hasta);

    List<EtiquetaAgregadoDto> reportePorEtiqueta(LocalDateTime desde, LocalDateTime hasta);

    List<EtiquetaAsignadaDto> listarEtiquetas(Long idAlerta);

    List<EtiquetaAsignadaDto> agregarEtiqueta(Long idAlerta, Long idEtiqueta);

    List<EtiquetaAsignadaDto> quitarEtiqueta(Long idAlerta, Long idEtiqueta);
}