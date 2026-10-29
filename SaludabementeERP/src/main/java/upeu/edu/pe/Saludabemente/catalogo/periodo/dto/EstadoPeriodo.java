package upeu.edu.pe.Saludabemente.catalogo.periodo.dto;

/**
 * Estados del ciclo de vida de un periodo academico/campana.
 *
 * <p>Vive en {@code dto} — no en {@code entity} — porque el modulo consumidor
 * (Evaluacion Nutricional) necesita hacer switch sobre este valor. Si viviera
 * en {@code entity}, habria que exponer el paquete completo, incluida la
 * entidad {@code Periodo}.</p>
 */
public enum EstadoPeriodo {
    PLANIFICADO,
    VIGENTE,
    CERRADO
}