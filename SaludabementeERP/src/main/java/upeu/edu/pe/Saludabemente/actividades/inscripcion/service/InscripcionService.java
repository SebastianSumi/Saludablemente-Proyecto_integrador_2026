package upeu.edu.pe.Saludabemente.actividades.inscripcion.service;

import upeu.edu.pe.Saludabemente.actividades.inscripcion.dto.response.InscritoDetalleResponse;

import java.util.List;

public interface InscripcionService {

    /**
     * Inscribe a TODAS las personas activas del programa.
     * Reemplaza los inscritos previos de la actividad.
     */
    int inscribirATodos(Long idActividad);

    /**
     * Inscribe a un conjunto especifico de personas.
     * Agrega a los existentes (no reemplaza).
     */
    int inscribirPersonas(Long idActividad, List<Long> idsPersonas);

    /**
     * Quita un inscrito.
     */
    boolean quitarInscrito(Long idActividad, Long idPersona);

    /**
     * Lista los IDs de personas inscritas.
     */
    List<Long> listarInscritos(Long idActividad);

    /**
     * Lista los inscritos con el detalle de cada persona
     * (nombre, area, celular, etc.), resolviendo sus datos via Personal.
     */
    List<InscritoDetalleResponse> listarInscritosDetalle(Long idActividad);

    /**
     * Indica si la persona esta inscrita en la actividad.
     */
    boolean estaInscrito(Long idActividad, Long idPersona);

    /**
     * Cuenta los inscritos.
     */
    long contarInscritos(Long idActividad);
}