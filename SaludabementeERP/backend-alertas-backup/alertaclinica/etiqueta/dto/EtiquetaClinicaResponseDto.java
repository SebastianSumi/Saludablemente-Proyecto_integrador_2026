package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EtiquetaClinicaResponseDto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String colorHex,
        String estado,
        LocalDateTime fechaBaja,
        List<ParametroFotocopiaDto> parametros,
        List<AlertaResumenParaEtiquetaDto> alertas
) {}
