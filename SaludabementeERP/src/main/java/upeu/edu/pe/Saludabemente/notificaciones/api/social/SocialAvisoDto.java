package upeu.edu.pe.Saludabemente.notificaciones.api.social;

import java.util.UUID;

/** idPersonaDestinatario ya resuelto por identidad. Solo aviso in-app, sin email. */
public record SocialAvisoDto(UUID idEvento, TipoAvisoSocial tipo, Long idPersonaDestinatario,
                            Long idPost, Long idComentario, Long idEquipo) {}
