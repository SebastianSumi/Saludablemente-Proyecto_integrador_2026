package upeu.edu.pe.Saludabemente.asistencias.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaQrCoordinadorRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionLoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaHistorialResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.CierreAsistenciasResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.MarcacionLoteResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.service.AsistenciaService;

import java.util.List;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualDetalleRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaActividadResponse;

@Tag(name = "Asistencias", description = "Registro y consulta de asistencias")
@RestController
@RequestMapping("/api/v1/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @Operation(summary = "Registrar o editar asistencia manual con detalle por persona (atómico, solo EN_CURSO)")
    @PostMapping("/manual/detalle")
    public ResponseEntity<AsistenciaActividadResponse> guardarManualDetalle(
            @Valid @RequestBody AsistenciaManualDetalleRequest request) {
        return ResponseEntity.ok(asistenciaService.guardarManualDetalle(request));
    }

    @Operation(summary = "Consultar asistencia agrupada por actividad con detalle e IDs individuales")
    @GetMapping("/manual/actividad/{idActividad}")
    public ResponseEntity<AsistenciaActividadResponse> consultarDetalle(@PathVariable Long idActividad) {
        return ResponseEntity.ok(asistenciaService.consultarDetalle(idActividad));
    }

    @Operation(summary = "Registrar asistencia manual (coordinador)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asistencia registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Actividad o persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio violada")
    })
    @PostMapping("/manual")
    public ResponseEntity<AsistenciaResponse> registrarManual(
            @Valid @RequestBody AsistenciaManualRequest request) {
        AsistenciaResponse response = asistenciaService.registrarManual(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar asistencias de una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista devuelta"),
            @ApiResponse(responseCode = "400", description = "Parámetro inválido")
    })
    @GetMapping("/actividad/{idActividad}")
    public ResponseEntity<List<AsistenciaResponse>> listarPorActividad(
            @PathVariable Long idActividad) {
        return ResponseEntity.ok(asistenciaService.listarPorActividad(idActividad));
    }

    @Operation(summary = "Historial de asistencia de una persona")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial devuelto"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    })
    @GetMapping("/historial/{idPersona}")
    public ResponseEntity<List<AsistenciaHistorialResponse>> historialPorPersona(
            @PathVariable Long idPersona) {
        return ResponseEntity.ok(asistenciaService.historialPorPersona(idPersona));
    }

    @Operation(summary = "Registrar asistencia por QR personal (coordinador)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asistencia registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Actividad o persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio violada (QR inválido, duplicado, etc.)")
    })
    @PostMapping("/qr/coordinador")
    public ResponseEntity<AsistenciaResponse> registrarPorQrCoordinador(
            @Valid @RequestBody AsistenciaQrCoordinadorRequest request) {
        AsistenciaResponse response = asistenciaService.registrarPorQrCoordinador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Forzar cierre de una actividad (admin)")
    @PostMapping("/cierre/{idActividad}")
    public ResponseEntity<CierreAsistenciasResponse> forzarCierre(
            @PathVariable Long idActividad) {
        return ResponseEntity.ok(asistenciaService.generarCierreDeAsistencias(idActividad));
    }

    @Operation(summary = "Marcación del wizard (upsert presente/tarde/falta, solo EN_CURSO)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marca aplicada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Actividad o persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio violada")
    })
    @PostMapping("/marcacion")
    public ResponseEntity<AsistenciaResponse> marcar(
            @Valid @RequestBody MarcacionRequest request) {
        return ResponseEntity.ok(asistenciaService.marcar(request));
    }

    @Operation(summary = "Marcación masiva del wizard (todos PRESENTE, solo EN_CURSO)")
    @PostMapping("/marcacion/lote")
    public ResponseEntity<MarcacionLoteResponse> marcarLote(
            @Valid @RequestBody MarcacionLoteRequest request) {
        return ResponseEntity.ok(asistenciaService.marcarLote(request));
    }
}
