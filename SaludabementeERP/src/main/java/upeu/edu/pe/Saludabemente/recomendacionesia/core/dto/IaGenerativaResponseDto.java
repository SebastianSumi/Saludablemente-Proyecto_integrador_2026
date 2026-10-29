package upeu.edu.pe.Saludabemente.recomendacionesia.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IaGenerativaResponseDto {

    private String model;
    private String createdAt;
    private Message message;
    private boolean done;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }

    public String getContenido() {
        return message != null ? message.getContent() : null;
    }
}