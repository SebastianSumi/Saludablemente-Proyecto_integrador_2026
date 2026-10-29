package upeu.edu.pe.Saludabemente.seguridad.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.VerificarRegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.VerificarRegistroResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.RegistroService;

/**
 * Autorregistro público de cuentas.
 *
 * <p>Endpoint público (sin token). El rol se fija en servidor
 * (Colaborador): el cliente nunca elige roles.</p>
 */
@Tag(name = "Registro público", description = "Autorregistro de cuentas vinculadas a una persona")
@RestController
@RequestMapping("/api/v1/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @Operation(summary = "Paso 1: verifica documento y email sin crear la cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Persona verificada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya tiene cuenta, email en uso o persona inactiva")
    })
    @PostMapping("/verificar")
    public ResponseEntity<VerificarRegistroResponse> verificar(
            @Valid @RequestBody VerificarRegistroRequest request) {
        return ResponseEntity.ok(registroService.verificar(request));
    }

    @Operation(summary = "Paso 2: crea la cuenta con rol Colaborador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya tiene cuenta o email en uso")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroService.registrar(request));
    }
}
