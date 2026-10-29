package upeu.edu.pe.Saludabemente.personal.persona.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.personal.credencial.dto.response.CredencialResponse;
import upeu.edu.pe.Saludabemente.personal.credencial.service.CredencialService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonalAgregado;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.CelularUpdateRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.request.PreferenciaRequest;
import upeu.edu.pe.Saludabemente.personal.preferencia.dto.response.PreferenciaResponse;
import upeu.edu.pe.Saludabemente.personal.preferencia.service.PreferenciaService;

import java.util.List;
import java.util.Set;

@Tag(name = "Personal")
@RestController
@RequestMapping("/api/v1/personal")
@RequiredArgsConstructor
public class PersonaController {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "id", "nombres", "apellidoPaterno", "apellidoMaterno",
            "celular", "fechaNacimiento", "activo"
    );

    private final PersonaService personaService;
    private final PreferenciaService preferenciaService;
    private final CredencialService credencialService;

    // -------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------

    @Operation(summary = "Lista personas con filtros combinables y ordenamiento opcional")
    @GetMapping
    public ResponseEntity<List<PersonaResponse>> buscar(
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long equipoId,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "apellidoPaterno") String ordenarPor,
            @RequestParam(defaultValue = "ASC") String direccion) {

        Sort sort = construirSort(ordenarPor, direccion);
        return ResponseEntity.ok(
                personaService.buscar(estado, sexo, areaId, equipoId, texto, sort));
    }

    @Operation(summary = "Reporte agregado del modulo Personal")
    @GetMapping("/resumen")
    public ResponseEntity<PersonalAgregado> resumen() {
        return ResponseEntity.ok(personaService.reporte());
    }

    @Operation(summary = "Consulta una persona por id")
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtener(id));
    }

    @Operation(summary = "Lista el resumen de personas de un equipo")
    @GetMapping("/equipo/{idEquipo}")
    public ResponseEntity<List<PersonaResumen>> listarPorEquipo(@PathVariable Long idEquipo) {
        return ResponseEntity.ok(personaService.listarResumenPorEquipo(idEquipo));
    }

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    @Operation(summary = "Registra una persona nueva")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PersonaResponse> crear(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.crear(request));
    }

    @Operation(summary = "Actualiza todos los datos de una persona")
    @PutMapping("/{id}")
    public ResponseEntity<PersonaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @Operation(summary = "Actualiza solo el celular de una persona")
    @PatchMapping("/{id}/celular")
    public ResponseEntity<PersonaResponse> cambiarCelular(
            @PathVariable Long id,
            @Valid @RequestBody CelularUpdateRequest request) {
        return ResponseEntity.ok(personaService.cambiarCelular(id, request.celular()));
    }

    @Operation(summary = "Inactiva una persona (baja logica)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivar(@PathVariable Long id) {
        personaService.inactivar(id);
    }

    @Operation(summary = "Reactiva una persona inactiva (emite nueva credencial QR)")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<PersonaResponse> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.reactivar(id));
    }

    @Operation(summary = "Sube o reemplaza la foto de perfil de una persona")
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PersonaResponse> subirAvatar(
            @PathVariable Long id,
            @RequestPart("avatar") MultipartFile avatar) {
        return ResponseEntity.ok(personaService.subirAvatar(id, avatar));
    }

    @Operation(summary = "Elimina la foto de perfil de una persona")
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<PersonaResponse> eliminarAvatar(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.eliminarAvatar(id));
    }

    // -------------------------------------------------------------------
    // Relaciones
    // -------------------------------------------------------------------

    @Operation(summary = "Obtiene las preferencias de comunicacion de una persona")
    @GetMapping("/{id}/preferencias")
    public ResponseEntity<PreferenciaResponse> obtenerPreferencias(@PathVariable Long id) {
        return ResponseEntity.ok(preferenciaService.obtenerPorPersona(id));
    }

    @Operation(summary = "Configura las preferencias de comunicacion de una persona")
    @PutMapping("/{id}/preferencias")
    public ResponseEntity<PreferenciaResponse> upsertPreferencias(
            @PathVariable Long id,
            @Valid @RequestBody PreferenciaRequest request) {
        return ResponseEntity.ok(preferenciaService.upsertPreferencias(id, request));
    }

    @Operation(summary = "Genera o rota la credencial QR de una persona")
    @PostMapping("/{id}/credencial")
    public ResponseEntity<CredencialResponse> generarCredencial(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean incluirQr) {
        return ResponseEntity.ok(credencialService.generarCredencial(id, incluirQr));
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private Sort construirSort(String ordenarPor, String direccion) {
        if (!CAMPOS_ORDENABLES.contains(ordenarPor)) {
            throw new IllegalArgumentException(
                    "Campo de ordenamiento no permitido: " + ordenarPor
                            + ". Permitidos: " + CAMPOS_ORDENABLES);
        }
        Sort.Direction dir = "DESC".equalsIgnoreCase(direccion)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(dir, ordenarPor);
    }
}