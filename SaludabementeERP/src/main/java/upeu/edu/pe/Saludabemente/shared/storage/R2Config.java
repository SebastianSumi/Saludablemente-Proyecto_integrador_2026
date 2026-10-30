package upeu.edu.pe.Saludabemente.shared.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    @ConditionalOnProperty(prefix = "saludablemente.storage.r2", name = "enabled", havingValue = "true")
    public S3Client r2S3Client(R2Properties properties) {
        String endpoint = resolverEndpoint(properties);
        exigir(endpoint, "R2_ENDPOINT o R2_ACCOUNT_ID");
        exigir(properties.getAccessKeyId(), "R2_ACCESS_KEY_ID");
        exigir(properties.getSecretAccessKey(), "R2_SECRET_ACCESS_KEY");
        exigir(properties.getBucket(), "R2_BUCKET");

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(StringUtils.hasText(properties.getRegion())
                        ? properties.getRegion().trim()
                        : "auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKeyId().trim(),
                                properties.getSecretAccessKey().trim())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    private static String resolverEndpoint(R2Properties properties) {
        if (StringUtils.hasText(properties.getEndpoint())) {
            return properties.getEndpoint().trim();
        }
        if (StringUtils.hasText(properties.getAccountId())) {
            return "https://" + properties.getAccountId().trim()
                    + ".r2.cloudflarestorage.com";
        }
        return null;
    }

    private static void exigir(String valor, String variable) {
        if (!StringUtils.hasText(valor)) {
            throw new IllegalStateException(
                    "Falta configurar " + variable + " para usar Cloudflare R2");
        }
    }
}
