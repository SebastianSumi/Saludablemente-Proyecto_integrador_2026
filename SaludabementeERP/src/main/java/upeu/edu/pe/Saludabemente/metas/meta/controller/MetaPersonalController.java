package upeu.edu.pe.Saludabemente.metas.meta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.ProgresoMetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;

import java.net.URI;
import java.util.List;

@Slf4j
@Tag(name = "Metas Personales")
@RestController
@RequestMapping("/api/v1/metas/personales")
@RequiredArgsConstructor
public class MetaPersonalController {

    private final MetaService metaService;
    private final AuditoriaMetasService auditoriaMetas;
    private final MetasUsuarioApi seguridadApi;

    @Operation(summary = "Crea una meta personal del colaborador autenticado")
    @PostMapping
    public ResponseEntity<MetaResponseDTO> crear(@Valid @RequestBody MetaRequestDTO dto) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador")) {
            throw new BusinessRuleException("No autorizado para crear metas personales");
        }
        UsuarioActualDTO usuario = seguridadApi.obtenerUsuarioActual();

        MetaResponseDTO creada = metaService.crearPersonal(dto, usuario.email());

        try {
            auditoriaMetas.registrarMetaCreada(
                    usuario.idUsuario(),
                    creada.id(),
                    creada.tipoOrigen().name(),
                    creada.pilar().name());
        } catch (RuntimeException ex) {
            log.warn("Fallo al auditar creacion de meta {}: {}",
                    creada.id(), ex.getMessage());
        }

        return ResponseEntity
                .created(URI.create("/api/v1/metas/personales/" + creada.id()))
                .body(creada);
    }

    @Operation(summary = "Lista las metas de un colaborador, opcionalmente filtradas por estado")
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<List<MetaResponseDTO>> listar(
            @PathVariable Long idPersona,
            @RequestParam(required = false) EstadoMeta estado) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador", "Evaluador")) {
            throw new BusinessRuleException("No autorizado para consultar metas");
        }
        return ResponseEntity.ok(metaService.listarPorPersona(idPersona, estado));
    }

    @Operation(summary = "Consulta el progreso de una meta (porcentaje y racha)")
    @GetMapping("/{idMeta}/progreso")
    public ResponseEntity<ProgresoMetaResponseDTO> progreso(@PathVariable Long idMeta) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador", "Evaluador")) {
            throw new BusinessRuleException("No autorizado para consultar progreso");
        }
        return ResponseEntity.ok(metaService.obtenerProgreso(idMeta));
    }
}