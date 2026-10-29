package upeu.edu.pe.Saludabemente.teams.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

final class TeamImagenValidator {
    private TeamImagenValidator() {}
    static void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > 5L * 1024 * 1024)
            throw new BusinessRuleException("Selecciona una imagen de hasta 5 MB");
        try (var input = archivo.getInputStream()) {
            byte[] b = input.readNBytes(12);
            String tipo = archivo.getContentType();
            boolean png = b.length >= 8 && Arrays.equals(Arrays.copyOf(b, 8), new byte[]{(byte)137,80,78,71,13,10,26,10});
            boolean jpg = b.length >= 3 && b[0] == (byte)255 && b[1] == (byte)216 && b[2] == (byte)255;
            boolean webp = b.length == 12 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            String extension = png ? ".png" : jpg ? ".jpg" : webp ? ".webp" : "";
            String nombre = archivo.getOriginalFilename();
            boolean extensionValida = nombre != null && (nombre.toLowerCase(java.util.Locale.ROOT).endsWith(extension)
                    || (jpg && nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".jpeg")));
            if (extension.isEmpty() || !extensionValida || !(png && "image/png".equals(tipo)
                    || jpg && "image/jpeg".equals(tipo) || webp && "image/webp".equals(tipo)))
                throw new BusinessRuleException("Formato permitido: JPEG, PNG o WebP; contenido y extensión deben coincidir");
        } catch (IOException ex) { throw new BusinessRuleException("No se pudo leer la imagen"); }
    }
}
