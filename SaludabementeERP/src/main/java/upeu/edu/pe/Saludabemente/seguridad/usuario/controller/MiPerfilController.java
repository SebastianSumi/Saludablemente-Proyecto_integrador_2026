package upeu.edu.pe.Saludabemente.seguridad.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.seguridad.api.SeguridadService;
import upeu.edu.pe.Saludabemente.seguridad.api.UsuarioPerfilDTO;

@Tag(name = "Mi Perfil", description = "Datos del usuario autenticado")
@RestController
@RequestMapping("/api/v1/mi-perfil")
@RequiredArgsConstructor
public class MiPerfilController {

    private final SeguridadService seguridadService;

    @Operation(summary = "Devuelve el perfil del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil devuelto"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping
    public ResponseEntity<UsuarioPerfilDTO> obtener() {
        return ResponseEntity.ok(seguridadService.obtenerMiPerfil());
    }
}