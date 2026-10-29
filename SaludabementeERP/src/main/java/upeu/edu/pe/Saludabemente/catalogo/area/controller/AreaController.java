package upeu.edu.pe.Saludabemente.catalogo.area.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;

import java.util.List;

@Tag(name = "Catálogo - Áreas de Trabajo")
@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "Lista areas, opcionalmente filtradas por estado")
    @GetMapping
    public ResponseEntity<List<AreaResponse>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "ASC") String orden) {

        Sort.Direction dir = "DESC".equalsIgnoreCase(orden)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(dir, "nombre");

        return ResponseEntity.ok(areaService.buscar(activo, sort));
    }

    @Operation(summary = "Lista el resumen de areas activas (para combos)")
    @GetMapping("/resumen")
    public ResponseEntity<List<AreaResumen>> listarResumen() {
        return ResponseEntity.ok(areaService.listarResumen());
    }

    @Operation(summary = "Obtiene un area por id")
    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(areaService.obtener(id));
    }

    @Operation(summary = "Registra un area nueva")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AreaResponse> crear(@Valid @RequestBody AreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.crear(request));
    }

    @Operation(summary = "Actualiza un area existente")
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AreaRequest request) {
        return ResponseEntity.ok(areaService.actualizar(id, request));
    }

    @Operation(summary = "Inactiva un area (baja logica)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivar(@PathVariable Long id) {
        areaService.inactivar(id);
    }
}