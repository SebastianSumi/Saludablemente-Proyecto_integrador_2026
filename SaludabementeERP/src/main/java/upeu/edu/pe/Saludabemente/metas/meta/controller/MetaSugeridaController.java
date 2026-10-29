package upeu.edu.pe.Saludabemente.metas.meta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaSugeridaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;

import java.net.URI;

@Slf4j
@Tag(name = "Metas Sugeridas (IA)")
@RestController
@RequestMapping("/api/v1/metas/sugeridas")
@RequiredArgsConstructor
public class MetaSugeridaController {

    private final MetaService metaService;
    private final AuditoriaMetasService auditoriaMetas;
    private final MetasUsuarioApi seguridadApi;

    @Operation(summary = "Crea una meta aceptada desde el módulo Recomendaciones IA")
    @PostMapping
    public ResponseEntity<MetaResponseDTO> crear(
            @Valid @RequestBody MetaSugeridaRequestDTO dto) {

        if (!seguridadApi.tieneAlgunRol("Colaborador", "Administrador")) {
            throw new BusinessRuleException(
                    "No autorizado para crear metas sugeridas");
        }
        UsuarioActualDTO usuario = seguridadApi.obtenerUsuarioActual();

        MetaResponseDTO creada = metaService.crearDesdeSugerencia(dto, usuario.email());

        try {
            auditoriaMetas.registrarMetaCreada(
                    usuario.idUsuario(),
                    creada.id(),
                    creada.tipoOrigen().name(),
                    creada.pilar().name());
        } catch (RuntimeException ex) {
            log.warn("Fallo al auditar creacion de meta sugerida {}: {}",
                    creada.id(), ex.getMessage());
        }

        return ResponseEntity
                .created(URI.create("/api/v1/metas/personales/" + creada.id()))
                .body(creada);
    }
}