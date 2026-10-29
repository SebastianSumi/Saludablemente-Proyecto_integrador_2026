package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaCarrera400mResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service.ReglaCarrera400mService;

import java.util.List;

@Tag(name = "Reglas Carrera 400m (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/carrera-400m")
@RequiredArgsConstructor
public class ReglaCarrera400mController {

    private final ReglaCarrera400mService service;

    @Operation(summary = "Lista reglas de carrera 400m con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaCarrera400mResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de carrera 400m por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaCarrera400mResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de carrera 400m")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaCarrera400mResponseDto crear(
            @Valid @RequestBody ReglaCarrera400mRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de carrera 400m existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaCarrera400mResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaCarrera400mRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de carrera 400m")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de carrera 400m")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaCarrera400mResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}