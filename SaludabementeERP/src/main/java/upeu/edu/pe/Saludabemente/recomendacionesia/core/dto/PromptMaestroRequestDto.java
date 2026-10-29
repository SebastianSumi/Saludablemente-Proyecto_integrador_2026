package upeu.edu.pe.Saludabemente.recomendacionesia.core.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa la petición completa que se envía al proveedor de IA.
 * Contiene el system prompt y el user prompt ya ensamblados,
 * más el schema JSON que la respuesta debe respetar.
 */
@Getter
@Setter
@Builder
public class PromptMaestroRequestDto {

    private String systemPrompt;
    private String userPrompt;
    private String jsonSchema;
}