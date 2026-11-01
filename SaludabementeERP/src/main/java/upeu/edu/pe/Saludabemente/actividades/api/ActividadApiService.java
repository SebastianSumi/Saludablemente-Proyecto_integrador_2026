package upeu.edu.pe.Saludabemente.actividades.api;

import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato publico del modulo Actividades para otros modulos.
 * Sin eventos: todo se resuelve por consulta directa (pull).
 */
public interface ActividadApiService {

    /**
     * Snapshot de una actividad. Devuelve null si no existe.
     * NO lanza excepcion.
     */
    ActividadSnapshotDto obtenerSnapshot(Long idActividad);

    /**
     * Todas las actividades en estado EN_CURSO.
     * Nunca devuelve null. Lista vacia si no hay.
     */
    List<ActividadSnapshotDto> listarEnCurso();

    /**
     * Actividades cuya fecha_actividad este en [desde, hasta].
     * Nunca devuelve null.
     */
    List<ActividadSnapshotDto> listarPorRango(LocalDate desde, LocalDate hasta);

    /**
     * Actividades en las que la persona esta inscrita, con fecha_actividad
     * dentro de [desde, hasta]. Nunca devuelve null.
     */
    List<ActividadSnapshotDto> listarPorPersona(Long idPersona, LocalDate desde, LocalDate hasta);

    /**
     * Snapshots en batch. Solo devuelve los IDs que existen.
     * Si ids es null o vacio, devuelve lista vacia.
     */
    List<ActividadSnapshotDto> obtenerSnapshots(List<Long> ids);

    /**
     * Actividades FINALIZADAS con cierreAsistenciasRealizado = false o null.
     * Asistencias lo consume con su propio cron.
     * Nunca devuelve null.
     */
    List<ActividadSnapshotDto> listarFinalizadasNoCerradas();

    /**
     * IDs de personas inscritas a una actividad.
     * Si la actividad no tiene inscritos explicitos, devuelve null
     * para que Asistencias use "todo el personal activo".
     */
    List<Long> listarInscritos(Long idActividad);

    /**
     * Indica si la persona esta inscrita explicitamente en la actividad.
     * Devuelve false si la actividad o la persona no existen, o si no hay
     * una fila explicita en ACTIVIDAD_INSCRITO.
     */
    boolean estaInscrito(Long idActividad, Long idPersona);

    /**
     * Marca una actividad como "cierre de asistencias ya procesado".
     * Asistencias la llama despues de generar el cierre.
     */
    void marcarCierreRealizado(Long idActividad);
}