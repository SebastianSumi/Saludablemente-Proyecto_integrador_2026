package upeu.edu.pe.Saludabemente.shared.storage.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import java.util.*;

/** Reglas locales de Social; no altera el almacenamiento de otros consumidores. */
@Service
@RequiredArgsConstructor
public class SocialComentarioStorageService implements AdjuntoSocialStorageApi {
    private static final Set<String> MIME = Set.of("image/jpeg", "image/png", "image/webp");
    private final AlmacenamientoImagenService almacenamiento;

    @Override
    public void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new BusinessRuleException("El adjunto es obligatorio");
        if (archivo.getSize() > MAX_BYTES) throw new BusinessRuleException("El adjunto supera 5 MiB");
        if (archivo.getContentType() == null || !MIME.contains(archivo.getContentType()))
            throw new BusinessRuleException("Solo JPEG, PNG o WebP");
        String nombre = archivo.getOriginalFilename();
        if (nombre != null && nombre.contains(".")) {
            String extension = nombre.substring(nombre.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (!Set.of(".jpg", ".jpeg", ".png", ".webp").contains(extension))
                throw new BusinessRuleException("Extensión de adjunto no permitida");
        }
    }

    @Override
    public AdjuntoSocialDto guardar(Long idPost, MultipartFile archivo) {
        String prefijo = prefijo(idPost);
        validar(archivo);
        String key = almacenamiento.guardar(archivo, prefijo, MAX_BYTES);
        return new AdjuntoSocialDto(key, almacenamiento.construirUrlPublica(key),
                archivo.getContentType(), archivo.getSize());
    }

    @Override
    public void validarReferencias(Long idPost, List<String> urls) {
        String prefijo = prefijo(idPost);
        if (urls == null || urls.isEmpty()) return;
        if (urls.size() > MAX_ADJUNTOS) throw new BusinessRuleException("Máximo cuatro adjuntos");
        String base = almacenamiento.construirUrlPublica(prefijo);
        if (base == null || base.isBlank()) throw new BusinessRuleException("URL pública no disponible");
        for (String url : urls) {
            if (url == null || !url.startsWith(base)) throw new BusinessRuleException("Adjunto ajeno al post");
            String archivo = url.substring(base.length());
            if (!archivo.matches("[A-Za-z0-9][A-Za-z0-9_-]*(?:\\.(?i:jpg|jpeg|png|webp))?"))
                throw new BusinessRuleException("Referencia de adjunto inválida");
        }
        if (new HashSet<>(urls).size() != urls.size()) throw new BusinessRuleException("Adjuntos duplicados");
    }

    private String prefijo(Long idPost) {
        if (idPost == null || idPost <= 0) throw new BusinessRuleException("Post inválido");
        return "social/comentarios/" + idPost + "/";
    }
}
