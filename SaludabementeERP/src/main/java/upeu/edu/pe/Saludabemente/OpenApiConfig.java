package upeu.edu.pe.Saludabemente;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_BEARER = "bearerAuth";

    @Bean
    OpenAPI saludablementeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Saludablemente API")
                        .version("v1")
                        .description("Contrato REST del backend de Saludablemente")
                        .contact(new Contact()
                                .name("Equipo Churucutitos Dev")
                                .url("https://github.com/SebastianSumi/Proyecto_integrador_2026")))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT emitido por el backend. "
                                                + "Obtener en POST /api/v1/auth/login y pegar como 'Bearer <token>'.")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_BEARER));
    }
}