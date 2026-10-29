package upeu.edu.pe.Saludabemente.social.post.service;
import upeu.edu.pe.Saludabemente.social.post.dto.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
public interface PostService {
    Long crear(CrearPostRequest request,SocialUsuarioActualDto usuario);
    void editar(Long id,EditarPostRequest request,SocialUsuarioActualDto usuario);
    void eliminar(Long id,SocialUsuarioActualDto usuario);
}
