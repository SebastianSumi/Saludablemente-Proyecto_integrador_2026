package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClinicaResponse {
    private Long idAlerta;
    private Long personaId;
    private Long evaluacionOrigenId;
    private String tipoEvaluacionOrigen;
    private Integer puntajeTotal;
    private String nivelSeveridad;
    private String mensajeResumen;
    private String estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaAtencion;
    private Long usuarioAtiendeId;
    private String observacionAtencion;
    private List<AlertaClinicaDetalleResponse> detalles;
    private List<EtiquetaAsignadaDto> etiquetas;
}