package upeu.edu.pe.Saludabemente.recomendacionesia.core.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IaResponseValidator {

    private static final List<String> NODOS_REQUERIDOS = List.of(
            "interpretacion_inteligente",
            "comparacion_historica",
            "objetivos_inteligentes",
            "sistema_puntuacion",
            "recomendacion_nutricional",
            "recomendacion_ejercicio",
            "plan_seguimiento",
            "prediccion_progreso"
    );

    private final ObjectMapper objectMapper;

    public void validar(String jsonResultado) {
        if (jsonResultado == null || jsonResultado.isBlank()) {
            throw new IllegalArgumentException("Respuesta de IA vacía");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(jsonResultado);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "La respuesta de la IA no es JSON válido: " + ex.getMessage(), ex);
        }

        for (String nodo : NODOS_REQUERIDOS) {
            if (!root.has(nodo)) {
                throw new IllegalArgumentException("Falta el nodo requerido: " + nodo);
            }
        }

        log.debug("Respuesta de IA validada correctamente. 8 nodos presentes.");
    }
}