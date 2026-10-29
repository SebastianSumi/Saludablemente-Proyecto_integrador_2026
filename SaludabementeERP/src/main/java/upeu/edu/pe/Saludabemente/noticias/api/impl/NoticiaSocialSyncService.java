package upeu.edu.pe.Saludabemente.noticias.api.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.noticias.api.social.SocialNoticiaApi;
import upeu.edu.pe.Saludabemente.social.api.SocialApiService;
import upeu.edu.pe.Saludabemente.social.api.dto.NoticiaSocialDto;

@Service @RequiredArgsConstructor
public class NoticiaSocialSyncService {
    private final SocialNoticiaApi fuente;
    private final SocialApiService social;
    public void sincronizar(Long id) {
        var noticia=fuente.obtenerNoticiaPorId(id);
        if(noticia.isEmpty() || !noticia.get().oficial()) {social.ocultarPostDeNoticia(id);return;}
        var n=noticia.get();social.sincronizarPostDeNoticia(new NoticiaSocialDto(n.idNoticia(),n.titulo(),n.resumen(),n.imagenPortadaUrl(),n.fechaPublicacion(),n.estado(),n.oficial(),n.idUsuarioAutor(),n.idEquipoAutor(),n.urlCompleta()));
    }
}
