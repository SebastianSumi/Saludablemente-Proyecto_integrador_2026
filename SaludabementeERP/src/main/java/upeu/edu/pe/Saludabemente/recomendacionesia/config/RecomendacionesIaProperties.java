package upeu.edu.pe.Saludabemente.recomendacionesia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "saludablemente.recomendaciones")
public class RecomendacionesIaProperties {

    private Ia ia = new Ia();
    private Outbox outbox = new Outbox();

    @Getter
    @Setter
    public static class Ia {
        private String provider;
        private Double temperature;
        private Integer numPredict;
        private Integer numCtx;
        private Gemini gemini = new Gemini();
    }

    @Getter
    @Setter
    public static class Gemini {
        private String model;
        private String readTimeout;
        private String apiKey;   // <-- Añadido
    }

    @Getter
    @Setter
    public static class Outbox {
        private Long schedulerIntervalMs;
        private Integer maxIntentos;
        private Long backoffInicialMs;
        private Integer backoffMultiplicador;
    }
}