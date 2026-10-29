package upeu.edu.pe.Saludabemente.teams.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.teams.dto.response.TeamResponse;
import upeu.edu.pe.Saludabemente.teams.service.TeamService;

import java.util.List;

@Tag(name = "Teams")
@RestController
@RequestMapping("/api/v1/equipos")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/mi-equipo")
    public TeamResponse miEquipo() { return teamService.miEquipo(); }

    @GetMapping("/mi-equipo/miembros")
    public List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> miembrosMiEquipo() {
        return teamService.miembrosMiEquipo();
    }

    @GetMapping("/{id}/puede-editar")
    public boolean puedeEditar(@PathVariable Long id) { return teamService.puedeEditar(id); }

    @GetMapping("/{id}/candidatos-lider")
    public List<upeu.edu.pe.Saludabemente.teams.api.TeamAccesoApi.CandidatoLider> candidatos(@PathVariable Long id) {
        return teamService.candidatos(id);
    }

    public record LiderRequest(@jakarta.validation.constraints.Positive Long idUsuario) {}

    @PutMapping("/{id}/lider")
    public TeamResponse asignarLider(@PathVariable Long id, @Valid @RequestBody LiderRequest request) {
        return teamService.asignarLider(id, request.idUsuario());
    }

    @PostMapping(value = "/{id}/imagen", consumes = "multipart/form-data")
    public TeamResponse subirImagen(@PathVariable Long id,
            @RequestPart("archivo") org.springframework.web.multipart.MultipartFile archivo) {
        return teamService.subirImagen(id, archivo);
    }

    @Operation(summary = "Lista equipos, opcionalmente solo los activos")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> listar(
            @RequestParam(required = false) Boolean activo) {
        Sort sort = Sort.by(Sort.Direction.ASC, "nombre");
        return ResponseEntity.ok(teamService.listar(activo, sort));
    }

    @Operation(summary = "Consulta un equipo por id")
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.obtener(id));
    }

    @Operation(summary = "Registra un equipo nuevo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TeamResponse> crear(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.crear(request));
    }

    @Operation(summary = "Actualiza un equipo existente")
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.actualizar(id, request));
    }

    @Operation(summary = "Inactiva un equipo (baja lógica)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivar(@PathVariable Long id) {
        teamService.inactivar(id);
    }
}
