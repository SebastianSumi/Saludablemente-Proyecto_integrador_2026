package upeu.edu.pe.Saludabemente.seguridad.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;      // ← IMPORT NUEVO
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.PasswordRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RolesRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.UsuarioService;

@Tag(name = "Usuarios", description = "Gestión de cuentas y roles del sistema")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // -------------------------------------------------------------------
    // Crear
    // -------------------------------------------------------------------

    @Operation(summary = "Crear una cuenta de usuario",
            description = "Captura el snapshot de identidad desde el módulo Personal y persiste la cuenta con contraseña cifrada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Email duplicado, persona ya con cuenta, persona inexistente o inactiva")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------
    // Buscar / obtener
    // -------------------------------------------------------------------

    @Operation(summary = "Buscar usuarios con filtros y paginación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado paginado"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación u ordenamiento inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> buscar(
            @Parameter(description = "Filtro parcial por email (LIKE, case-insensitive)")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filtro por estado de cuenta (true=activo, false=inactivo)")
            @RequestParam(required = false) Boolean activo,

            @ParameterObject                                                   // ← FIX
            @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {

        return ResponseEntity.ok(usuarioService.buscar(email, activo, pageable));
    }

    @Operation(summary = "Obtener el perfil 360° de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    // -------------------------------------------------------------------
    // Actualizar roles
    // -------------------------------------------------------------------

    @Operation(summary = "Reemplazar los roles de un usuario",
            description = "Sobrescribe por completo la asignación de roles. Debe quedar al menos uno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles actualizados"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Roles inexistentes o lista vacía")
    })
    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioResponse> actualizarRoles(
            @PathVariable Long id,
            @Valid @RequestBody RolesRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarRoles(id, request));
    }

    // -------------------------------------------------------------------
    // Actualizar contraseña
    // -------------------------------------------------------------------

    @Operation(summary = "Cambiar la contraseña de un usuario",
            description = "La nueva contraseña se cifra con BCrypt antes de persistir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> actualizarPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordRequest request) {
        usuarioService.actualizarPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------
    // Inactivar / reactivar
    // -------------------------------------------------------------------

    @Operation(summary = "Inactivar una cuenta (baja lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta inactivada"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "La cuenta ya está inactiva")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inactivar(@PathVariable Long id) {
        usuarioService.inactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivar una cuenta inactivada")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta reactivada"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "La cuenta ya está activa")
    })
    @PostMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        usuarioService.reactivar(id);
        return ResponseEntity.noContent().build();
    }
}