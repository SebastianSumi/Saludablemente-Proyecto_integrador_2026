package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import java.util.List;
public record EtiquetaParametrosResponse(Long idEtiqueta, String revision, List<ParametroFotocopiaDto> detalles) {}
