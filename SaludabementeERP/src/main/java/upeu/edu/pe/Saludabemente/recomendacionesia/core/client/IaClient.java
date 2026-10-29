package upeu.edu.pe.Saludabemente.recomendacionesia.core.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import upeu.edu.pe.Saludabemente.recomendacionesia.config.RecomendacionesIaProperties;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.IaGenerativaResponseDto;
import upeu.edu.pe.Saludabemente.recomendacionesia.exception.IaConexionException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente de IA generativa. Desde la limpieza de servicios externos solo
 * se soporta Gemini (API key en header); Ollama fue retirado.
 */
@Slf4j
@Component
public class IaClient {

    private final RecomendacionesIaProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient geminiClient;

    public IaClient(RecomendacionesIaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory geminiFactory = new JdkClientHttpRequestFactory(httpClient);
        geminiFactory.setReadTimeout(Duration.ofSeconds(120));
        this.geminiClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(geminiFactory)
                .build();
    }

    public IaGenerativaResponseDto ejecutarPrompt(String systemPrompt,
                                                  String userPrompt,
                                                  String jsonSchema) {
        String provider = properties.getIa().getProvider();
        if (!"gemini".equalsIgnoreCase(provider)) {
            throw new IaConexionException(
                    "Proveedor de IA no soportado: " + provider
                            + ". Configure saludablemente.recomendaciones.ia.provider=gemini");
        }
        return llamarGemini(systemPrompt, userPrompt, jsonSchema);
    }

    private IaGenerativaResponseDto llamarGemini(String systemPrompt,
                                                 String userPrompt,
                                                 String jsonSchema) {
        try {
            String promptCombinado = systemPrompt + "\n\n---\n\n" + userPrompt;

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", properties.getIa().getTemperature());
            generationConfig.put("maxOutputTokens", properties.getIa().getNumPredict());
            if (jsonSchema != null && !jsonSchema.isBlank()) {
                generationConfig.put("responseMimeType", "application/json");
                generationConfig.put("responseSchema", objectMapper.readTree(jsonSchema));
            }

            Map<String, Object> part = Map.of("text", promptCombinado);
            Map<String, Object> content = Map.of("parts", List.of(part));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(content));
            body.put("generationConfig", generationConfig);

            String modelName = properties.getIa().getGemini().getModel();
            String apiKey = properties.getIa().getGemini().getApiKey();
            String uri = "/v1beta/models/" + modelName + ":generateContent";

            log.info("Llamando a Gemini. Modelo={}", modelName);

            JsonNode root = geminiClient.post()
                    .uri(uri)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null) throw new IaConexionException("Respuesta vacía de Gemini");

            JsonNode candidates = root.get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
                throw new IaConexionException("Gemini no devolvió candidates");
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts == null || !parts.isArray() || parts.isEmpty()) {
                throw new IaConexionException("Gemini no devolvió parts");
            }
            String texto = parts.get(0).path("text").asText();
            if (texto == null || texto.isBlank()) {
                throw new IaConexionException("Gemini devolvió texto vacío");
            }

            IaGenerativaResponseDto dto = new IaGenerativaResponseDto();
            dto.setModel(modelName);
            IaGenerativaResponseDto.Message msg = new IaGenerativaResponseDto.Message();
            msg.setRole("assistant");
            msg.setContent(texto);
            dto.setMessage(msg);
            dto.setDone(true);
            return dto;

        } catch (IaConexionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IaConexionException("Error al comunicarse con Gemini: " + ex.getMessage(), ex);
        }
    }
}
