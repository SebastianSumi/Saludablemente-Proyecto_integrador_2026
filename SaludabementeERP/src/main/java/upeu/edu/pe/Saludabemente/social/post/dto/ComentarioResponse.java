package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record ComentarioResponse(Long idComentario, Long idComentarioPadre, Long idUsuarioAutor, AutorResponse autor, String contenido, Boolean editado, LocalDateTime fechaEdicion, LocalDateTime fechaComentario, List<String> adjuntosUrls, List<MencionResponse> menciones, List<String> hashtags, ReaccionResumenResponse reacciones, List<ComentarioResponse> respuestas, boolean puedoEditar, boolean puedoEliminar, Long idPost, boolean eliminado) {}
