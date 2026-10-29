package upeu.edu.pe.Saludabemente.metas.meta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaClinicaRequestDTO;
import upeu.edu.pe.Saludabemente.metas.meta.dto.MetaResponseDTO;
import upeu.edu.pe.Saludabemente.metas.meta.service.AuditoriaMetasService;
import upeu.edu.pe.Saludabemente.metas.meta.service.MetaService;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.MetasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.metas.UsuarioActualDTO;

import java.net.URI;

@Slf4j
@Tag(name = "Metas Clinicas")
@RestController
@RequestMapping("/api/v1/metas/clinicas")
@RequiredArgsConstructor
public class MetaClinicaController {

    private final MetaService metaService;
    private final AuditoriaMetasService auditoriaMetas;
    private final MetasUsuarioApi seguridadApi;

    @Operation(summary = "Prescribe una meta clinica vinculada a una evaluacion y alerta")
    @PostMapping
    public ResponseEntity<MetaResponseDTO> prescribir(
            @Valid @RequestBody MetaClinicaRequestDTO dto) {

        if (!seguridadApi.tieneAlgunRol("Evaluador", "Administrador")) {
            throw new BusinessRuleException("No autorizado para prescribir metas clinicas");
        }
        UsuarioActualDTO usuario = seguridadApi.obtenerUsuarioActual();

        MetaResponseDTO creada = metaService.crearClinica(dto, usuario.email());

        try {
            auditoriaMetas.registrarMetaCreada(
                    usuario.idUsuario(),
                    creada.id(),
                    creada.tipoOrigen().name(),
                    creada.pilar().name());
        } catch (RuntimeException ex) {
            log.warn("Fallo al auditar prescripcion de meta clinica {}: {}",
                    creada.id(), ex.getMessage());
        }

        return ResponseEntity
                .created(URI.create("/api/v1/metas/clinicas/" + creada.id()))
                .body(creada);
    }
}