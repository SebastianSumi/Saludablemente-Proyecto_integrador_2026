package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Fechas del reporte referidas a fechaAsignacion; cantidades calculadas sobre todo el filtro. */
public record ReporteEtiquetasResponse(
        long cantidadEtiquetas, long etiquetasActivas, long cantidadAsociaciones,
        long alertasUnicas, long alertasCriticasUnicas, List<Fila> detalles) {
    public record Fila(Long idEtiqueta, String codigo, String nombre, String colorHex, String estado,
                       List<ParametroFotocopiaDto> parametros, long cantidadAlertas,
                       LocalDateTime ultimaAsignacion, List<AlertaResumenParaEtiquetaDto> alertas) {}
}
