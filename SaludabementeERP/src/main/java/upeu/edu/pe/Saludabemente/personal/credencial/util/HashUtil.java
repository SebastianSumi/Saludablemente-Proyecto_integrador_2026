package upeu.edu.pe.Saludabemente.personal.credencial.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utilidad de hashing para credenciales QR.
 *
 * Combina SHA-256 con un salt institucional leído de variable de entorno.
 * Sin el salt, un atacante con acceso a la BD no puede generar QRs válidos.
 *
 * No es una clase final con métodos estáticos porque necesita inyectar el salt
 * desde la configuración de Spring. Se registra como @Component.
 */
@Component
public class HashUtil {

    private final String salt;

    public HashUtil(@Value("${saludablemente.security.qr-salt}") String salt) {
        this.salt = salt;
    }

    /**
     * Genera el hash SHA-256 de (salt + token).
     * Devuelve la representación hexadecimal en minúsculas.
     */
    public String generarHashSeguro(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo o vacío");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String entrada = salt + token;
            byte[] hashBytes = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en cualquier JVM; esto nunca debería ocurrir
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}