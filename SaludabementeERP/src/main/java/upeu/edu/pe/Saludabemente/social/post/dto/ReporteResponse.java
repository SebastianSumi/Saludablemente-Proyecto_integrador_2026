package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record ReporteResponse(Long idReporte, Long idPost, Long idComentario, AutorResponse reportante, MotivoReporte motivo, String detalle, LocalDateTime fechaReporte, EstadoReporte estado, AutorResponse revisor, LocalDateTime fechaRevision, String accionTomada, String comentarioRevision) {}