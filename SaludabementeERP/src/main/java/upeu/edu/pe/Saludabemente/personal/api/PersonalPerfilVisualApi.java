package upeu.edu.pe.Saludabemente.personal.api;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Datos visuales actuales para Seguridad. Su implementacion no depende de los
 * servicios de gestion de Persona ni de Teams, que requieren autorizacion.
 */
public interface PersonalPerfilVisualApi {
    /** Personas activas, maximo 100 IDs; el resultado se identifica por idPersona. */
    List<PersonaAvatarDTO> obtenerAvatares(List<Long> idsPersona);
    PersonalPerfilVisualDTO obtenerPerfilVisual(Long idPersona);
    PersonalPerfilVisualDTO actualizarPresentacionSocial(Long idPersona, String presentacion);
    PersonalPerfilVisualDTO subirFotoPerfilSocial(Long idPersona, MultipartFile archivo, boolean portada);
}
