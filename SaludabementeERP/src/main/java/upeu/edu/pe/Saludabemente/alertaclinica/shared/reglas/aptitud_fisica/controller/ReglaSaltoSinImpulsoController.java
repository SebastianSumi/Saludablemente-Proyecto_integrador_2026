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
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoRequestDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.dto.ReglaSaltoSinImpulsoResponseDto;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.service.ReglaSaltoSinImpulsoService;

import java.util.List;

@Tag(name = "Reglas Salto Sin Impulso (Administracion)")
@RestController
@RequestMapping("/api/v1/reglas/salto-sin-impulso")
@RequiredArgsConstructor
public class ReglaSaltoSinImpulsoController {

    private final ReglaSaltoSinImpulsoService service;

    @Operation(summary = "Lista reglas de salto sin impulso con filtros opcionales")
    @GetMapping
    public ResponseEntity<List<ReglaSaltoSinImpulsoResponseDto>> listar(
            @RequestParam(required = false) EstadoRegla estado,
            @RequestParam(required = false) SexoBiologico sexo) {
        return ResponseEntity.ok(service.listar(estado, sexo));
    }

    @Operation(summary = "Consulta una regla de salto sin impulso por id")
    @GetMapping("/{id}")
    public ResponseEntity<ReglaSaltoSinImpulsoResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @Operation(summary = "Registra una regla de salto sin impulso")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReglaSaltoSinImpulsoResponseDto crear(
            @Valid @RequestBody ReglaSaltoSinImpulsoRequestDto requestDto) {
        return service.crear(requestDto);
    }

    @Operation(summary = "Actualiza una regla de salto sin impulso existente")
    @PutMapping("/{id}")
    public ResponseEntity<ReglaSaltoSinImpulsoResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReglaSaltoSinImpulsoRequestDto requestDto) {
        return ResponseEntity.ok(service.actualizar(id, requestDto));
    }

    @Operation(summary = "Desactiva una regla de salto sin impulso")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }

    @Operation(summary = "Reactiva una regla de salto sin impulso")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ReglaSaltoSinImpulsoResponseDto> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivar(id));
    }
}