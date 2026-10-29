package upeu.edu.pe.Saludabemente.seguridad.rol.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.Saludabemente.seguridad.rol.dto.response.RolResponse;
import upeu.edu.pe.Saludabemente.seguridad.rol.service.RolService;

import java.util.List;

/**
 * Endpoints REST del catálogo de roles.
 *
 * <p><b>Alcance Sub-fase 1:</b> listado completo sin filtro fantasma.
 * En Sub-fase 2, si el solicitante no es ROOT, se ocultará el rol ROOT
 * del catálogo.</p>
 */
@Tag(name = "Roles", description = "Catálogo de roles de negocio")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar el catálogo de roles disponibles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo devuelto")
    })
    @GetMapping
    public ResponseEntity<List<RolResponse>> listarTodos() {
        return ResponseEntity.ok(rolService.listarTodos());
    }
}