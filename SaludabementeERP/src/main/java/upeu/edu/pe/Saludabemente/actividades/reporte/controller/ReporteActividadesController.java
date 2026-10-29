package upeu.edu.pe.Saludabemente.actividades.reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReporteActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.reporte.dto.response.ReportePeriodoResponse;
import upeu.edu.pe.Saludabemente.actividades.reporte.service.ReporteActividadesService;

import java.time.LocalDate;

@Tag(name = "Actividades - Reportes")
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteActividadesController {

    private final ReporteActividadesService reporteActividadesService;

    @Operation(summary = "Reporte A: actividad con su configuracion completa (materiales + publico objetivo)")
    @GetMapping("/actividades/{id}")
    public ResponseEntity<ReporteActividadResponse> reporteActividad(@PathVariable Long id) {
        return ResponseEntity.ok(reporteActividadesService.reporteActividad(id));
    }

    @Operation(summary = "Reporte B: agenda agrupada por periodo (cabecera: conteos, detalle: actividades)")
    @GetMapping("/agenda")
    public ResponseEntity<ReportePeriodoResponse> reportePeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String periodo) {
        return ResponseEntity.ok(reporteActividadesService.reportePeriodo(fechaInicio, fechaFin, periodo));
    }
}