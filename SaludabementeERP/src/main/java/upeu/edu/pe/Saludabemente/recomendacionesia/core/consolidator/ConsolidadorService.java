package upeu.edu.pe.Saludabemente.recomendacionesia.core.consolidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionIa;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.entity.RecomendacionPendiente;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.repository.RecomendacionIaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsolidadorService {

    private static final List<String> NODOS_NUTRICION = List.of(
            "interpretacion_inteligente",
            "objetivos_inteligentes",
            "sistema_puntuacion",
            "recomendacion_nutricional"
    );

    private static final List<String> NODOS_EJERCICIO = List.of(
            "comparacion_historica",
            "recomendacion_ejercicio",
            "plan_seguimiento",
            "prediccion_progreso"
    );

    private final RecomendacionIaRepository recomendacionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void consolidar(RecomendacionPendiente pendiente) {

        String jsonGrande = pendiente.getJsonResultado();
        if (jsonGrande == null || jsonGrande.isBlank()) {
            throw new IllegalStateException("No hay JSON para consolidar");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(jsonGrande);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "JSON de la IA no parseable: " + ex.getMessage(), ex);
        }

        String bloqueNutricion = extraerBloque(root, NODOS_NUTRICION);
        String bloqueEjercicio  = extraerBloque(root, NODOS_EJERCICIO);

        int desactivadas = recomendacionRepository
                .desactivarVigentesDePersona(pendiente.getIdPersona());
        log.info("Desactivadas {} recomendaciones previas de persona={}",
                desactivadas, pendiente.getIdPersona());

        guardar(pendiente, "Nutrición", bloqueNutricion);
        guardar(pendiente, "Ejercicio",  bloqueEjercicio);

        log.info("Consolidación completada para persona={} evaluacion={}",
                pendiente.getIdPersona(), pendiente.getIdEvaluacion());
    }

    private String extraerBloque(JsonNode root, List<String> nodos) {
        ObjectNode bloque = objectMapper.createObjectNode();
        for (String nodo : nodos) {
            bloque.set(nodo, root.get(nodo));
        }
        return objectMapper.writeValueAsString(bloque);
    }

    private void guardar(RecomendacionPendiente pendiente, String tipo, String contenido) {
        RecomendacionIa entity = new RecomendacionIa();
        entity.setIdPersona(pendiente.getIdPersona());
        entity.setIdEvaluacion(pendiente.getIdEvaluacion());
        entity.setTipo(tipo);
        entity.setContenidoGenerado(contenido);
        entity.setPromptContexto(pendiente.getPromptEnviado());
        entity.setModeloIaUsado(pendiente.getModeloIaUsado());
        entity.setFechaGeneracion(LocalDateTime.now());
        entity.setVigente(1);
        recomendacionRepository.save(entity);
    }
}