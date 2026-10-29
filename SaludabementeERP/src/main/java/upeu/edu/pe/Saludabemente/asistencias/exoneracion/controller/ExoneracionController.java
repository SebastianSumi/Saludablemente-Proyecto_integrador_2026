package upeu.edu.pe.Saludabemente.asistencias.exoneracion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoJustificacion;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.DictamenRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.request.JustificacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.dto.response.JustificacionResponse;
import upeu.edu.pe.Saludabemente.asistencias.exoneracion.service.ExoneracionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Exoneraciones", description = "Justificaciones de ausencia")
@RestController
@RequestMapping("/api/v1/exoneraciones")
@RequiredArgsConstructor
public class ExoneracionController {

    private final ExoneracionService exoneracionService;

    @Operation(summary = "Solicitar justificación de ausencia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud registrada"),
            @ApiResponse(responseCode = "404", description = "Persona o actividad no encontrada"),
            @ApiResponse(responseCode = "409", description = "No hay ausencia o ya existe solicitud pendiente")
    })
    @PostMapping("/solicitar")
    public ResponseEntity<JustificacionResponse> solicitar(
            @Valid @RequestBody JustificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exoneracionService.solicitar(request));
    }

    @Operation(summary = "Dictaminar justificación (APROBADO o RECHAZADO)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dictamen registrado"),
            @ApiResponse(responseCode = "404", description = "Justificación no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya dictaminada o estado inválido")
    })
    @PutMapping("/{id}/dictaminar")
    public ResponseEntity<JustificacionResponse> dictaminar(
            @PathVariable Long id,
            @Valid @RequestBody DictamenRequest request) {
        return ResponseEntity.ok(exoneracionService.dictaminar(id, request));
    }

    @Operation(summary = "Listar justificaciones con filtros")
    @GetMapping
    public ResponseEntity<Page<JustificacionResponse>> listar(
            @RequestParam(required = false) EstadoJustificacion estado,
            @RequestParam(required = false) Long idPersona,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("fechaSolicitud").descending());

        return ResponseEntity.ok(exoneracionService.listar(
                estado, idPersona, desde, hasta, pageable));
    }

    @Operation(summary = "Detectar ausentismo crónico")
    @GetMapping("/ausentismo")
    public ResponseEntity<Map<String, Object>> detectarAusentismo(
            @RequestParam(defaultValue = "3") int minRacha) {
        List<Long> ids = exoneracionService.detectarAusentismoCronico(minRacha);
        return ResponseEntity.ok(Map.of(
                "minRacha", minRacha,
                "total", ids.size(),
                "idsPersona", ids
        ));
    }
}