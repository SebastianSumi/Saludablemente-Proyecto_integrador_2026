package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record PostDetalleResponse(Long idPost, String tipoContenido, Long refId, Long idEquipoAutor, String titulo, String resumen, String lugar, LocalDate fechaEvento, String horaInicio, String horaFin, String estado, String imagenUrl, List<String> imagenes, String coordinadorNombre, Boolean esOficial, LocalDateTime fechaPublicacion, LocalDateTime fechaActualizacion, AutorResponse autor, AutorResponse autorEquipo, Long alcanceTotal, long totalComentarios, long totalShares, List<ComentarioResponse> comentarios, ReaccionResumenResponse reacciones, PostResumenResponse postOriginal, List<MencionResponse> menciones, List<String> hashtags, SocialEquipoPerfilDto equipo, boolean puedoEditar, boolean puedoEliminar) {}