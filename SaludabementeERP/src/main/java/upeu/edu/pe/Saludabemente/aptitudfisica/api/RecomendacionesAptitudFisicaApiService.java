package upeu.edu.pe.Saludabemente.aptitudfisica.api;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Funciones de lectura que Aptitud Física expone a Recomendaciones IA.
 *
 * <p>Reglas del contrato v1.1:</p>
 * <ul>
 *   <li>Lecturas devuelven {@code Optional.empty()} ante ausencia, sin excepción.</li>
 *   <li>{@code null} en un campo de prueba significa "no medido", no {@code 0}.</li>
 *   <li>La validación de condiciones la hace el Core de Recomendaciones IA.
 *       Aptitud Física solo responde a las consultas.</li>
 * </ul>
 */
public interface RecomendacionesAptitudFisicaApiService {

    /**
     * Trae la evaluación física de una persona en una fecha exacta.
     * Devuelve {@code Optional.empty()} si no existe registro para esa
     * combinación. Acepta cualquier estado de evaluación: si está
     * {@code PENDIENTE}, las pruebas no medidas viajan en {@code null}.
     */
    Optional<AptitudFisicaDto> obtenerPorPersonaYFecha(Long idPersona, LocalDate fecha);

    /**
     * ¿Existe evaluación física en estado {@code COMPLETA} para esa persona
     * en ese período? {@code true} solo si está completa.
     */
    boolean existePruebaFisica(Long idPersona, Long idPeriodo);
}