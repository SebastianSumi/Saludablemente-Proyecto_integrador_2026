package upeu.edu.pe.Saludabemente.actividades.integracion.client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.actividades.api.social.SocialActividadApi;
import upeu.edu.pe.Saludabemente.social.api.SocialApiService;
import upeu.edu.pe.Saludabemente.social.api.dto.ActividadSocialDto;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import java.util.List;

/** Llamada directa dentro de la transacción del dueño; no captura fallos para evitar posts huérfanos. */
@Service @RequiredArgsConstructor
public class ActividadSocialSyncService {
    private final SocialActividadApi fuente;
    private final SocialApiService social;
    private final TeamApiService equipos;
    public void validarEquipo(Long id) {if(id!=null)equipos.validarEquipos(List.of(id));}
    public void sincronizar(Long id) {
        var d=fuente.obtenerPublicacion(id).orElseThrow(()->new ResourceNotFoundException("Actividad ausente para sincronización"));
        social.sincronizarPostDeActividad(new ActividadSocialDto(d.idActividad(),d.nombre(),d.descripcion(),d.lugar(),d.fechaActividad(),d.horaInicio(),d.horaFin(),d.estado(),d.logoUrl(),d.imagenesUrls(),d.nombreCoordinador(),d.idUsuarioCreador(),d.idEquipoActividad()));
    }
}
