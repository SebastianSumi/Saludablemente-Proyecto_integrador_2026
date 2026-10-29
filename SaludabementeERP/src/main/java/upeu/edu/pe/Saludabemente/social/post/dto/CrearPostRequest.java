package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record CrearPostRequest(@Size(max=150) String titulo, @Size(max=500) String resumen, Long idEquipoAutor, List<@Size(max=500) String> imagenesUrls) {}