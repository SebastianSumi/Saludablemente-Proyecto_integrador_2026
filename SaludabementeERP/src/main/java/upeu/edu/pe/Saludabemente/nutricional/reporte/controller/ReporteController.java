package upeu.edu.pe.Saludabemente.nutricional.reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.nutricional.reporte.service.ReporteService;

@Tag(name = "Reportes Nutricionales",
        description = "Exportación de consolidados y fichas clínicas")
@RestController
@RequestMapping("/api/v1/evaluaciones-nutricionales")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // ===================================================================
    // GET /exportar/{periodo}
    // ===================================================================

    @Operation(summary = "Exporta el consolidado del semestre en formato Excel",
            description = "Una fila por evaluación. Cruce por código de periodo.")
    @ApiResponse(responseCode = "200", description = "Archivo XLSX generado")
    @GetMapping("/exportar/{periodo}")
    public ResponseEntity<byte[]> exportarConsolidado(
            @Parameter(description = "Código del periodo (ej. 2026-1)", required = true)
            @PathVariable String periodo) {

        byte[] excel = reporteService.exportarConsolidadoSemestral(periodo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"consolidado-" + periodo + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // ===================================================================
    // GET /{id}/pdf
    // ===================================================================

    @Operation(summary = "Descarga la ficha clínica individual en PDF")
    @ApiResponse(responseCode = "200", description = "PDF generado")
    @ApiResponse(responseCode = "404", description = "Evaluación inexistente")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable Long id) {

        byte[] pdf = reporteService.generarFichaClinicaPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"ficha-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}