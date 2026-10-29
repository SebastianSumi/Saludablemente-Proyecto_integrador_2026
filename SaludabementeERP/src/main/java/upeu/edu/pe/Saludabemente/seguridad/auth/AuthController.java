package upeu.edu.pe.Saludabemente.seguridad.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.seguridad.auth.dto.LoginRequest;
import upeu.edu.pe.Saludabemente.seguridad.auth.dto.LoginResponse;

@Tag(name = "Autenticación", description = "Inicio de sesión con credenciales propias")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Inicia sesión y emite un JWT",
            description = "Acepta email o username. El token debe enviarse como Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o cuenta inactiva")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Cierra la sesión del cliente",
            description = "El JWT es stateless: el cliente debe descartar el token.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada en el cliente")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
