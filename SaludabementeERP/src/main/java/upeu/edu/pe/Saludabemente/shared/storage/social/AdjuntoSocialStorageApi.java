package upeu.edu.pe.Saludabemente.shared.storage.social;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AdjuntoSocialStorageApi {
    long MAX_BYTES = 5L * 1024 * 1024;
    int MAX_ADJUNTOS = 4;
    void validar(MultipartFile archivo);
    AdjuntoSocialDto guardar(Long idPost, MultipartFile archivo);
    /** Acepta solo URLs canónicas del servidor para el mismo post. */
    void validarReferencias(Long idPost, List<String> urls);
}
