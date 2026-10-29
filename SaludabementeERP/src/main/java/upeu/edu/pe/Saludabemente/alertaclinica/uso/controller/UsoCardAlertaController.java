package upeu.edu.pe.Saludabemente.alertaclinica.uso.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.dto.UsoCardRequest;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.service.UsoCardAlertaService;

import java.util.Map;

@Tag(name = "Alertas Clinicas - Uso de cards")
@RestController
@RequestMapping("/api/v1/alertaclinica/uso-cards")
@RequiredArgsConstructor
public class UsoCardAlertaController {

    private final UsoCardAlertaService service;

    @Operation(summary = "Frecuencia de uso de los cards del usuario autenticado")
    @GetMapping
    public ResponseEntity<Map<String, Integer>> obtener() {
        return ResponseEntity.ok(service.obtener());
    }

    @Operation(summary = "Registra el uso de un card")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrar(@Valid @RequestBody UsoCardRequest request) {
        service.registrar(request.ruta());
    }

    @Operation(summary = "Reinicia el orden (borra la frecuencia guardada)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reiniciar() {
        service.reiniciar();
    }
}
