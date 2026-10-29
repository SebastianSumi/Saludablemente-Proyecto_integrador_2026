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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaPlanchasResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service.ReglaPlanchasService;

import java.util.List;

@Tag(name = "Reglas Planchas (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/planchas")
@RequiredArgsConstructor
public class ReglaPlanchasController {

    private final ReglaPlanchasService service;

    @Operation(summary = "Lista reglas de planchas con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaPlanchasResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de planchas por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaPlanchasResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de planchas")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaPlanchasResponseDto crear(
            @Valid @RequestBody ReglaPlanchasRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de planchas existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaPlanchasResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaPlanchasRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de planchas")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de planchas")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaPlanchasResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}