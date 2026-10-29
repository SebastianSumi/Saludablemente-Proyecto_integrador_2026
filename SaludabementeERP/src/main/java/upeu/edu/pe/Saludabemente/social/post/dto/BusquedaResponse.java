package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record BusquedaResponse(String tipo, Page<PostResumenResponse> publicaciones, Page<ComentarioResponse> comentarios, List<AutorResponse> autores, List<String> hashtags) {}