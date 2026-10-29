package upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.request.ObservacionesRequest;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.response.EvaluacionAptitudResponse;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.AptitudAgregadoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.dto.resumen.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.service.EvaluacionAptitudService;

import java.util.List;

@Tag(name = "Aptitud Física")
@RestController
@RequestMapping("/api/v1/aptitud")
@RequiredArgsConstructor
public class EvaluacionAptitudController {

    private final EvaluacionAptitudService service;

    @Operation(summary = "Inicia una evaluación de aptitud (crea cabecera con snapshot)")
    @PostMapping
    public ResponseEntity<EvaluacionAptitudResponse> iniciar(
            @Valid @RequestBody EvaluacionInitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciar(request));
    }

    @Operation(summary = "Búsqueda paginada con filtros opcionales")
    @GetMapping
    public ResponseEntity<Page<EvaluacionResumen>> buscar(
            @RequestParam(required = false) Long idPeriodo,
            @RequestParam(required = false) EstadoEvaluacion estado,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String texto,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.buscar(idPeriodo, estado, area, texto, pageable));
    }

    @Operation(summary = "Obtiene la evaluación completa con detalle de fuerza y cardio")
    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionAptitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Historial de evaluaciones de una persona (orden descendente)")
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<List<EvaluacionResumen>> historial(@PathVariable Long idPersona) {
        return ResponseEntity.ok(service.historial(idPersona));
    }

    @Operation(summary = "Anula una evaluación (baja lógica)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        service.anular(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza las observaciones/restricciones de la cabecera")
    @PutMapping("/{id}/observaciones")
    public ResponseEntity<EvaluacionAptitudResponse> actualizarObservaciones(
            @PathVariable Long id, @Valid @RequestBody ObservacionesRequest request) {
        return ResponseEntity.ok(service.actualizarObservaciones(id, request));
    }

    @Operation(summary = "Métricas agregadas por período (para dashboard)")
    @GetMapping("/agregados/{idPeriodo}")
    public ResponseEntity<AptitudAgregadoDto> agregados(@PathVariable Long idPeriodo) {
        return ResponseEntity.ok(service.obtenerAgregados(idPeriodo));
    }
}