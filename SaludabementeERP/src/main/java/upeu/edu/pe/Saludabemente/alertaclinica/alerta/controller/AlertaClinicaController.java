package upeu.edu.pe.Saludabemente.alertaclinica.alerta.controller;

import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.AsociacionClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResponse;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaReporte;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AtenderAlertaRequest;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.EstadoAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.SeveridadAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.service.AlertaClinicaService;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.AgregarEtiquetaRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAgregadoDto;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaAsignadaDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Tag(name = "Alertas Clinicas")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "JSON, detalle o filtro invalido"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso inexistente"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto de negocio, revision desactualizada o bloqueo concurrente")
})
@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaClinicaController {
    private final AsociacionClinicaService asociaciones;


    private final AlertaClinicaService alertaService;

    @Operation(summary = "Lista alertas con filtros combinados, etiquetas y ordenamiento opcionales")
    @GetMapping
    public ResponseEntity<List<AlertaClinicaResponse>> buscar(
            @RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) SeveridadAlerta severidad,
            @RequestParam(required = false) Long personaId,
            @RequestParam(required = false) TipoEvaluacionOrigen tipoOrigen,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String etiqueta,
            @RequestParam(defaultValue = "fechaGeneracion") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion) {

        List<String> codigosEtiquetas = (etiqueta != null && !etiqueta.isBlank())
                ? Arrays.stream(etiqueta.split(","))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .toList()
                : null;

        return ResponseEntity.ok(alertaService.buscar(
                estado, severidad, personaId, tipoOrigen, desde, hasta,
                codigosEtiquetas, ordenarPor, direccion));
    }

    @Operation(summary = "Genera un reporte de alertas: agregados y detalle resumido")
    @GetMapping("/resumen")
    public ResponseEntity<AlertaReporte> resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(alertaService.reporte(desde, hasta));
    }

    @Operation(summary = "Reporte de alertas por etiqueta (para el panel del dashboard)")
    @GetMapping("/resumen/etiquetas")
    public ResponseEntity<List<EtiquetaAgregadoDto>> resumenPorEtiqueta(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(alertaService.reportePorEtiqueta(desde, hasta));
    }

    @Operation(summary = "Consulta una alerta por id, con sus detalles y etiquetas")
    @GetMapping("/{id}")
    public ResponseEntity<AlertaClinicaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.obtener(id));
    }

    @Operation(summary = "Marca una alerta como atendida")
    @PatchMapping("/{id}/atender")
    public ResponseEntity<AlertaClinicaResponse> atender(
            @PathVariable Long id,
            @Valid @RequestBody AtenderAlertaRequest request) {
        return ResponseEntity.ok(alertaService.atender(id, request));
    }

    @Operation(summary = "Anula (baja logica) una alerta")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<AlertaClinicaResponse> anular(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.anular(id));
    }

    @Operation(summary = "Lista las etiquetas asignadas a una alerta")
    @GetMapping("/{id}/etiquetas")
    public ResponseEntity<List<EtiquetaAsignadaDto>> listarEtiquetas(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.listarEtiquetas(id));
    }

    @Operation(summary = "Agrega una etiqueta a una alerta (asignacion MANUAL)")
    @PostMapping("/{id}/etiquetas")
    @ResponseStatus(HttpStatus.CREATED)
    public List<EtiquetaAsignadaDto> agregarEtiqueta(
            @PathVariable Long id,
            @Valid @RequestBody AgregarEtiquetaRequestDto request) {
        return alertaService.agregarEtiqueta(id, request.idEtiqueta());
    }

    @Operation(summary = "Quita una etiqueta de una alerta")
    @DeleteMapping("/{id}/etiquetas/{idEtiqueta}")
    public ResponseEntity<List<EtiquetaAsignadaDto>> quitarEtiqueta(
            @PathVariable Long id,
            @PathVariable Long idEtiqueta) {
        return ResponseEntity.ok(alertaService.quitarEtiqueta(id, idEtiqueta));
    }

    @Operation(summary = "Consulta la tarjeta editable de etiquetas con revision")
    @GetMapping("/{id}/etiquetas/edicion")
    public AlertaEtiquetasResponse etiquetasEdicion(@PathVariable Long id) { return asociaciones.etiquetasDeAlerta(id); }

    @Operation(summary = "Guarda todas las etiquetas de una alerta en una transaccion", description = "Enviar revision del GET /etiquetas/edicion y conjunto final completo, incluidas las MOTOR. No modifica puntaje, severidad ni indicadores.")
    @PutMapping("/{id}/etiquetas")
    public AlertaEtiquetasResponse guardarEtiquetas(@PathVariable Long id, @Valid @RequestBody GuardarEtiquetasRequest request) {
        return asociaciones.guardarEtiquetasDeAlerta(id, request);
    }

    @Operation(summary = "Busca alertas existentes resumidas y paginadas para seleccionar asociaciones")
    @GetMapping("/opciones")
    public org.springframework.data.domain.Page<upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto.AlertaClinicaResumen> opciones(
            @RequestParam(required = false) Long personaId,
            @RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) SeveridadAlerta severidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
        return alertaService.buscarOpciones(personaId, estado, severidad, desde, hasta, pagina, tamanio);
    }
}
