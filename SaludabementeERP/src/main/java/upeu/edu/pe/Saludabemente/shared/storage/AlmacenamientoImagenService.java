package upeu.edu.pe.Saludabemente.shared.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Punto único de almacenamiento de imágenes del ERP sobre Cloudflare R2.
 *
 * <p>Si R2 no está habilitado, las subidas se rechazan con un error de negocio
 * y las URLs absolutas ya persistidas se siguen devolviendo tal cual.</p>
 */
@Service
public class AlmacenamientoImagenService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoImagenService.class);
    private static final long MAX_BYTES_DEFAULT = 10L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/webp", ".webp",
            "image/gif", ".gif",
            "image/bmp", ".bmp");

    private final ObjectProvider<S3Client> s3Provider;
    private final R2Properties properties;

    public AlmacenamientoImagenService(ObjectProvider<S3Client> s3Provider,
                                       R2Properties properties) {
        this.s3Provider = s3Provider;
        this.properties = properties;
    }

    public String guardar(MultipartFile archivo) {
        return guardar(archivo, "", MAX_BYTES_DEFAULT);
    }

    public String guardar(MultipartFile archivo, String prefijo) {
        return guardar(archivo, prefijo, MAX_BYTES_DEFAULT);
    }

    public String guardar(MultipartFile archivo, String prefijo, long maxBytes) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessRuleException("La imagen es obligatoria");
        }
        if (archivo.getSize() > maxBytes) {
            throw new BusinessRuleException(
                    "La imagen supera el límite de " + (maxBytes / 1024 / 1024) + " MB");
        }
        String contentType = normalizarContentType(archivo.getContentType());
        String extension = EXTENSIONES.get(contentType);
        if (extension == null) {
            throw new BusinessRuleException(
                    "Formato de imagen no soportado. Use JPEG, PNG, WebP, GIF o BMP");
        }

        S3Client s3 = s3Provider.getIfAvailable();
        if (!properties.isEnabled() || s3 == null) {
            throw new BusinessRuleException(
                    "El almacenamiento de imágenes no está configurado. "
                            + "Defina R2_ENABLED, R2_ENDPOINT, R2_ACCESS_KEY_ID, "
                            + "R2_SECRET_ACCESS_KEY y R2_BUCKET.");
        }

        String objeto = construirClave(prefijo, extension);
        try {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(properties.getBucket().trim())
                            .key(objeto)
                            .contentType(contentType)
                            .contentLength(archivo.getSize())
                            .build(),
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));
        } catch (IOException ex) {
            throw new BusinessRuleException("No se pudo leer la imagen");
        } catch (SdkException ex) {
            log.error("Error subiendo imagen a Cloudflare R2 (bucket={}, key={})",
                    properties.getBucket(), objeto, ex);
            throw new BusinessRuleException("No se pudo subir la imagen al almacenamiento");
        }
        return objeto;
    }

    public void eliminar(String objetoNombre) {
        if (!StringUtils.hasText(objetoNombre)) {
            return;
        }
        S3Client s3 = s3Provider.getIfAvailable();
        if (!properties.isEnabled() || s3 == null) {
            return;
        }
        String clave = claveDesdeReferencia(objetoNombre);
        if (!StringUtils.hasText(clave) || clave.endsWith("/")) {
            return;
        }
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket().trim())
                    .key(clave)
                    .build());
        } catch (SdkException ex) {
            log.warn("No se pudo eliminar el objeto {} de Cloudflare R2", clave, ex);
        }
    }

    public String construirUrlPublica(String objetoNombre) {
        if (!StringUtils.hasText(objetoNombre)) {
            return null;
        }
        String valor = objetoNombre.trim();
        if (valor.startsWith("http://") || valor.startsWith("https://")
                || valor.startsWith("data:")) {
            return valor;
        }
        if (!StringUtils.hasText(properties.getPublicBaseUrl())) {
            return null;
        }
        boolean esPrefijo = valor.endsWith("/");
        String ruta = normalizarRuta(valor);
        if (!StringUtils.hasText(ruta)) {
            return null;
        }
        String url = quitarBarraFinal(properties.getPublicBaseUrl().trim())
                + "/" + codificarRuta(ruta);
        return esPrefijo ? url + "/" : url;
    }

    private String construirClave(String prefijo, String extension) {
        String ruta = normalizarRuta(prefijo);
        String nombre = UUID.randomUUID().toString().replace("-", "") + extension;
        return StringUtils.hasText(ruta) ? ruta + "/" + nombre : nombre;
    }

    private String claveDesdeReferencia(String referencia) {
        String valor = referencia.trim();
        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            String base = quitarBarraFinal(properties.getPublicBaseUrl() == null
                    ? "" : properties.getPublicBaseUrl().trim());
            if (StringUtils.hasText(base) && valor.startsWith(base + "/")) {
                return normalizarRuta(valor.substring(base.length()));
            }
            String bucket = properties.getBucket() == null ? "" : properties.getBucket().trim();
            String marca = "/" + bucket + "/";
            int indice = StringUtils.hasText(bucket) ? valor.indexOf(marca) : -1;
            if (indice >= 0) {
                return normalizarRuta(valor.substring(indice + marca.length()));
            }
            return null;
        }
        return normalizarRuta(valor);
    }

    private static String normalizarRuta(String valor) {
        if (!StringUtils.hasText(valor)) {
            return "";
        }
        String ruta = valor.trim().replace('\\', '/').replaceAll("/{2,}", "/");
        while (ruta.startsWith("/")) {
            ruta = ruta.substring(1);
        }
        while (ruta.endsWith("/")) {
            ruta = ruta.substring(0, ruta.length() - 1);
        }
        if (ruta.contains("..")) {
            throw new BusinessRuleException("Ruta de almacenamiento inválida");
        }
        return ruta;
    }

    private static String normalizarContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int separador = contentType.indexOf(';');
        String valor = separador >= 0 ? contentType.substring(0, separador) : contentType;
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private static String codificarRuta(String ruta) {
        StringBuilder resultado = new StringBuilder();
        String[] segmentos = ruta.split("/");
        for (int i = 0; i < segmentos.length; i++) {
            if (i > 0) {
                resultado.append('/');
            }
            resultado.append(URLEncoder.encode(segmentos[i], StandardCharsets.UTF_8)
                    .replace("+", "%20"));
        }
        return resultado.toString();
    }

    private static String quitarBarraFinal(String valor) {
        String resultado = valor;
        while (resultado.endsWith("/")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }
}
