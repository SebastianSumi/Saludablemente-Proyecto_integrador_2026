package upeu.edu.pe.Saludabemente.actividades.api.social;

import java.util.*;

public interface SocialActividadApi {
    Optional<ActividadSnapshotDto> obtenerActividadPorId(Long idActividad);
    /** Consulta vacía: lista vacía; límite efectivo 1–20. Incluye CANCELADA. */
    List<ActividadBusquedaDto> buscarActividades(String query, int limite);
    /** Solo atribución explícita ID_EQUIPO_ACTIVIDAD; no deriva equipo del creador. */
    List<ActividadSnapshotDto> actividadesPorEquipo(Long idEquipo);
    Optional<ActividadPublicacionDto> obtenerPublicacion(Long idActividad);
}
