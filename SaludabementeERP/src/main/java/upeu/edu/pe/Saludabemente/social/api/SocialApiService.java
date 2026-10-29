package upeu.edu.pe.Saludabemente.social.api;

import upeu.edu.pe.Saludabemente.social.api.dto.ActividadSocialDto;
import upeu.edu.pe.Saludabemente.social.api.dto.NoticiaSocialDto;

/**
 * Contrato de entrada a Social para los módulos dueños del contenido.
 * Los DTOs se ensamblan en el módulo de origen, sin exponer entidades JPA.
 * La implementación y su política transaccional corresponden a F3.
 */
public interface SocialApiService {
    void sincronizarPostDeActividad(ActividadSocialDto dto);

    void ocultarPostDeActividad(Long idActividad);

    void sincronizarPostDeNoticia(NoticiaSocialDto dto);

    void ocultarPostDeNoticia(Long idNoticia);
}
