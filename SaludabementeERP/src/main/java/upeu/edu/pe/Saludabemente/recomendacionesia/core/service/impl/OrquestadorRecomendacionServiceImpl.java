package upeu.edu.pe.Saludabemente.recomendacionesia.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.client.IaClient;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.consolidator.ConsolidadorService;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.IaGenerativaResponseDto;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.SnapshotDto;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.EstadoPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.prompt.JsonMaestroSchema;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.prompt.PromptAssembler;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.prompt.SystemPromptProvider;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.repository.RecomendacionPendienteRepository;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.service.OrquestadorRecomendacionService;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.service.SnapshotBuilderService;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.validator.IaResponseValidator;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrquestadorRecomendacionServiceImpl implements OrquestadorRecomendacionService {

    private final RecomendacionPendienteRepository pendienteRepository;
    private final SnapshotBuilderService snapshotBuilder;
    private final PromptAssembler promptAssembler;
    private final IaClient iaClient;
    private final IaResponseValidator validator;
    private final ConsolidadorService consolidadorService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void procesarPendiente(RecomendacionPendiente pendiente) {
        try {
            // 1. Marcar PROCESANDO
            pendiente.setEstado(EstadoPendiente.PROCESANDO);
            pendiente.setFechaUltimoIntento(LocalDateTime.now());
            pendiente.setIntentos(pendiente.getIntentos() + 1);
            pendienteRepository.save(pendiente);

            // 2. Snapshot
            SnapshotDto snapshot = snapshotBuilder.construir(
                    pendiente.getIdEvaluacion(), pendiente.getIdPersona());

            // 3. Prompts
            String systemPrompt = SystemPromptProvider.SYSTEM_PROMPT;
            String userPrompt = promptAssembler.armar(snapshot);

            // ── Llamada 1/2: Nutrición ──────────────────────────────
            log.info("Llamada 1/2 a IA: Nutrición");
            IaGenerativaResponseDto respNutricion = iaClient.ejecutarPrompt(
                    systemPrompt, userPrompt, JsonMaestroSchema.SCHEMA_NUTRICION);
            String jsonNutricion = respNutricion.getContenido();
            if (jsonNutricion == null || jsonNutricion.isBlank()) {
                throw new IllegalStateException("Respuesta vacía de la llamada 1 (Nutrición)");
            }

            // ── Llamada 2/2: Ejercicio ──────────────────────────────
            log.info("Llamada 2/2 a IA: Ejercicio");
            IaGenerativaResponseDto respEjercicio = iaClient.ejecutarPrompt(
                    systemPrompt, userPrompt, JsonMaestroSchema.SCHEMA_EJERCICIO);
            String jsonEjercicio = respEjercicio.getContenido();
            if (jsonEjercicio == null || jsonEjercicio.isBlank()) {
                throw new IllegalStateException("Respuesta vacía de la llamada 2 (Ejercicio)");
            }

            // ── Fusionar en un único JSON con los 8 nodos ───────────
            String jsonGrande = fusionarJson(jsonNutricion, jsonEjercicio);

            // ── Validar que los 8 nodos raíz estén presentes ────────
            validator.validar(jsonGrande);

            // 4. Guardar resultado
            pendiente.setJsonResultado(jsonGrande);
            pendiente.setPromptEnviado(systemPrompt + "\n\n---USER---\n\n" + userPrompt);
            pendiente.setModeloIaUsado(respNutricion.getModel());
            pendiente.setEstado(EstadoPendiente.COMPLETADO);
            pendiente.setFechaCompletado(LocalDateTime.now());
            pendiente.setUltimoError(null);
            pendienteRepository.save(pendiente);

            // 5. Consolidar
            consolidadorService.consolidar(pendiente);

            log.info("Pendiente {} procesado correctamente (2 llamadas)", pendiente.getIdPendiente());

        } catch (Exception ex) {
            log.error("Error procesando pendiente {}: {}", pendiente.getIdPendiente(), ex.getMessage(), ex);

            pendiente.setUltimoError(ex.getMessage());

            if (pendiente.getIntentos() >= pendiente.getMaxIntentos()) {
                pendiente.setEstado(EstadoPendiente.FALLIDO);
                log.warn("Pendiente {} agotó reintentos. Estado FALLIDO.", pendiente.getIdPendiente());
            } else {
                pendiente.setEstado(EstadoPendiente.PENDIENTE);
                log.info("Pendiente {} vuelve a PENDIENTE para reintento {}/{}",
                        pendiente.getIdPendiente(), pendiente.getIntentos(), pendiente.getMaxIntentos());
            }

            pendienteRepository.save(pendiente);
        }
    }

    /**
     * Fusiona los dos JSON (Nutrición + Ejercicio) en un único objeto
     * con los 8 nodos raíz.
     */
    private String fusionarJson(String jsonNutricion, String jsonEjercicio) {
        try {
            JsonNode nodoNutricion = objectMapper.readTree(jsonNutricion);
            JsonNode nodoEjercicio = objectMapper.readTree(jsonEjercicio);

            ObjectNode merged = objectMapper.createObjectNode();

            nodoNutricion.properties().forEach(entry ->
                    merged.set(entry.getKey(), entry.getValue()));

            nodoEjercicio.properties().forEach(entry ->
                    merged.set(entry.getKey(), entry.getValue()));

            return objectMapper.writeValueAsString(merged);

        } catch (Exception e) {
            throw new IllegalStateException("Error fusionando JSONs: " + e.getMessage(), e);
        }
    }
}