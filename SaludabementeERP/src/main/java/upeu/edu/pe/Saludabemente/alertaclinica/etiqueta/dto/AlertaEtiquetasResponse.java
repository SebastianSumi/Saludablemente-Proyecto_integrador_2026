package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import java.util.List;
public record AlertaEtiquetasResponse(Long idAlerta, String revision, List<EtiquetaAsignadaDto> detalles) {}
