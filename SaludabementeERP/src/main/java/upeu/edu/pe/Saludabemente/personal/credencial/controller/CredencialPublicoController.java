package upeu.edu.pe.Saludabemente.personal.credencial.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.personal.api.CredencialApiService;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;
import upeu.edu.pe.Saludabemente.personal.api.VerificarQrRequest;

/**
 * Endpoint HTTP publico para verificacion de QR.
 *
 * <p>Lo consume el frontend SPA de Asistencias tras escanear el QR de un
 * colaborador. Internamente delega en {@link CredencialApiService}.</p>
 *
 * <p>Nunca responde 4xx por QR invalido: siempre devuelve 200 con
 * {@code valido = false} y el mensaje. El consumidor decide que hacer.</p>
 */
@Tag(name = "Credenciales - Verificación QR")
@RestController
@RequestMapping("/api/v1/credenciales")
@RequiredArgsConstructor
public class CredencialPublicoController {

    private final CredencialApiService credencialApiService;

    @Operation(summary = "Verifica un codigo QR escaneado")
    @PostMapping("/verificar")
    public ResponseEntity<VerificacionQrResponse> verificar(
            @Valid @RequestBody VerificarQrRequest request) {
        return ResponseEntity.ok(
                credencialApiService.verificarQrParaAsistencia(request.codigoQrHash()));
    }
}