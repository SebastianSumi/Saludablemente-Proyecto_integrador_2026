package upeu.edu.pe.Saludabemente.catalogo.periodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResumen;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;

import java.util.List;

@Tag(name = "Catálogo - Periodos")
@RestController
@RequestMapping("/api/v1/periodos")
@RequiredArgsConstructor
public class PeriodoController {

    private final PeriodoService periodoService;

    @Operation(summary = "Lista periodos, opcionalmente filtrados por estado")
    @GetMapping
    public ResponseEntity<List<PeriodoResponse>> listar(
            @RequestParam(required = false) EstadoPeriodo estado,
            @RequestParam(defaultValue = "DESC") String orden,
            @RequestParam(defaultValue = "codigo") String ordenarPor) {

        Sort.Direction dir = "ASC".equalsIgnoreCase(orden)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, ordenarPor);

        return ResponseEntity.ok(periodoService.buscar(estado, sort));
    }

    @Operation(summary = "Lista el resumen de todos los periodos")
    @GetMapping("/resumen")
    public ResponseEntity<List<PeriodoResumen>> listarResumen() {
        return ResponseEntity.ok(periodoService.listarResumen());
    }

    @Operation(summary = "Obtiene el periodo actualmente VIGENTE")
    @GetMapping("/vigente")
    public ResponseEntity<PeriodoResponse> obtenerVigente() {
        return ResponseEntity.ok(periodoService.obtenerVigente());
    }

    @Operation(summary = "Obtiene un periodo por su codigo estandarizado (ej. 2026-1)")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PeriodoResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(periodoService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Obtiene un periodo por id")
    @GetMapping("/{id}")
    public ResponseEntity<PeriodoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(periodoService.obtener(id));
    }

    @Operation(summary = "Registra un periodo nuevo en estado PLANIFICADO")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PeriodoResponse> crear(@Valid @RequestBody PeriodoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodoService.crear(request));
    }

    @Operation(summary = "Actualiza un periodo existente (no cambia su estado)")
    @PutMapping("/{id}")
    public ResponseEntity<PeriodoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PeriodoRequest request) {
        return ResponseEntity.ok(periodoService.actualizar(id, request));
    }

    @Operation(summary = "Marca un periodo como VIGENTE (falla si ya hay otro VIGENTE)")
    @PatchMapping("/{id}/vigente")
    public ResponseEntity<PeriodoResponse> marcarVigente(@PathVariable Long id) {
        return ResponseEntity.ok(periodoService.marcarVigente(id));
    }

    @Operation(summary = "Cierra un periodo (estado CERRADO, sella el historial)")
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<PeriodoResponse> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(periodoService.cerrar(id));
    }

    @Operation(summary = "Elimina un periodo (baja logica: pasa a CERRADO)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        periodoService.eliminar(id);
    }
}