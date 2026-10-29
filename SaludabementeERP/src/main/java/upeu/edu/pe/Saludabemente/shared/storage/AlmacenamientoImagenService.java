package upeu.edu.pe.Saludabemente.shared.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;

/**
 * Punto único de almacenamiento de imágenes del ERP.
 *
 * <p>MinIO fue retirado del proyecto. La subida queda deshabilitada hasta
 * definir un reemplazo (disco local, S3 compatible u otro proveedor).
 * Las URLs absolutas ya persistidas se siguen devolviendo tal cual para no
 * romper datos históricos.</p>
 */
@Service
public class AlmacenamientoImagenService {

    private static final long MAX_BYTES_DEFAULT = 10L * 1024 * 1024;

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
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessRuleException("El archivo no es una imagen válida");
        }
        throw new BusinessRuleException(
                "El almacenamiento de imágenes no está disponible: MinIO fue retirado. "
                        + "Configure un proveedor de almacenamiento para habilitar esta función.");
    }

    public void eliminar(String objetoNombre) {
        // Sin proveedor de almacenamiento no hay objeto físico que eliminar.
    }

    public String construirUrlPublica(String objetoNombre) {
        if (objetoNombre == null || objetoNombre.isBlank()) {
            return null;
        }
        String valor = objetoNombre.trim();
        if (valor.startsWith("http://") || valor.startsWith("https://")
                || valor.startsWith("data:")) {
            return valor;
        }
        // Claves relativas de MinIO no se pueden resolver sin el servicio.
        return null;
    }
}
