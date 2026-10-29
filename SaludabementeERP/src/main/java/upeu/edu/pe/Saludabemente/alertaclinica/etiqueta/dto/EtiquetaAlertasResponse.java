package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import java.util.List;
public record EtiquetaAlertasResponse(Long idEtiqueta, String revision, List<AlertaResumenParaEtiquetaDto> detalles) {}
