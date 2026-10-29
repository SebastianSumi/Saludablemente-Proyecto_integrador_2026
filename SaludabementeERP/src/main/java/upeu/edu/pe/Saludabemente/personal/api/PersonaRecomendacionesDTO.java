package upeu.edu.pe.Saludabemente.personal.api;

import java.time.LocalDate;

/**
 * Datos demograficos de una persona, expuestos exclusivamente al modulo
 * Recomendaciones IA para contextualizar el prompt enviado al servicio
 * de IA generativa.
 *
 * <p><b>Sobre la edad</b>: este DTO expone {@code fechaNacimiento}, NO la
 * edad calculada. El consumidor calcula la edad al momento de la
 * evaluacion para que el contexto clinico sea exacto.</p>
 *
 * <p><b>Sobre {@code sexo}</b>: se expone como {@link String} ("M" / "F"),
 * no como el enum {@code Sexo} del paquete
 * {@code personal.persona.entity} (interno).</p>
 */
public record PersonaRecomendacionesDTO(
        Long idPersona,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String sexo,
        LocalDate fechaNacimiento,
        Boolean activo
) {
}