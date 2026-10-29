package upeu.edu.pe.Saludabemente.asistencias.cuarentena.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.request.ResolverCuarentenaRequest;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.dto.response.CuarentenaResponse;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.service.CuarentenaService;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.enums.MotivoCuarentena;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Cuarentena", description = "Bandeja de registros retenidos")
@RestController
@RequestMapping("/api/v1/cuarentena")
@RequiredArgsConstructor
public class CuarentenaController {

    private final CuarentenaService cuarentenaService;

    @Operation(summary = "Listar bandeja de cuarentena con filtros")
    @ApiResponse(responseCode = "200", description = "Página de resultados")
    @GetMapping
    public ResponseEntity<Page<CuarentenaResponse>> listar(
            @RequestParam(required = false) EstadoCuarentena estado,
            @RequestParam(required = false) MotivoCuarentena motivo,
            @RequestParam(required = false) Long idActividad,
            @RequestParam(required = false) Long idPersona,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = "ASC".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(cuarentenaService.listarBandeja(
                estado, motivo, idActividad, idPersona, desde, hasta, pageable));
    }

    @Operation(summary = "Obtener un registro de cuarentena por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    @GetMapping("/{idCuarentena}")
    public ResponseEntity<CuarentenaResponse> obtener(@PathVariable Long idCuarentena) {
        return ResponseEntity.ok(cuarentenaService.obtener(idCuarentena));
    }

    @Operation(summary = "Resolver un registro (APROBAR o RECHAZAR)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resuelto"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Ya resuelto o decisión inválida")
    })
    @PutMapping("/{idCuarentena}/resolver")
    public ResponseEntity<CuarentenaResponse> resolver(
            @PathVariable Long idCuarentena,
            @Valid @RequestBody ResolverCuarentenaRequest request) {
        return ResponseEntity.ok(cuarentenaService.resolver(idCuarentena, request));
    }

    @Operation(summary = "Contar registros pendientes")
    @GetMapping("/pendientes/count")
    public ResponseEntity<Map<String, Long>> contarPendientes() {
        return ResponseEntity.ok(Map.of("total", cuarentenaService.contarPendientes()));
    }
}