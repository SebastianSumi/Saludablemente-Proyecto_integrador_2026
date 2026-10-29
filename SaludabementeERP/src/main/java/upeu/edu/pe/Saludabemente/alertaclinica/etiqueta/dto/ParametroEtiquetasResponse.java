package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import java.util.List;
public record ParametroEtiquetasResponse(Long idParametro, String revision, List<EtiquetaResumenDto> detalles) {}
