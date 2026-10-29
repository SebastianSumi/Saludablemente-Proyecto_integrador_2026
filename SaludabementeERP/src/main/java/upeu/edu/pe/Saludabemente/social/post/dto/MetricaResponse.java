package upeu.edu.pe.Saludabemente.social.post.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoPerfilDto;
public record MetricaResponse(long totalPosts, long totalComentarios, long totalReacciones, long totalShares, long usuariosActivos, double engagement, List<TopPost> topComentados, List<TopPost> topReaccionados, List<TopUsuario> topUsuarios, List<TopHashtag> hashtagsTendencia, List<VolumenDiario> volumenPorDia) {}