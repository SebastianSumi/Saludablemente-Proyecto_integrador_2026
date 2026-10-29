package upeu.edu.pe.Saludabemente.personal.api;

import java.time.LocalDate;

/**
 * Contexto demografico minimo de una persona, expuesto exclusivamente al
 * modulo Alertas Clinicas para el motor de reglas OMS/OMRON.
 *
 * <p>Alertas usa {@code sexo} para elegir la tabla de referencia y
 * {@code fechaNacimiento} para calcular la edad a la fecha de la evaluacion
 * (no a la fecha actual).</p>
 *
 * <p><b>Sobre {@code sexo}</b>: se expone como {@link String} ("M" / "F"),
 * no como el enum {@code Sexo} de {@code personal.persona.entity} (paquete
 * interno no expuesto).</p>
 *
 * <p><b>Sobre {@code nombreCompleto}</b>: se calcula en Personal con el
 * formato "ApellidoPaterno ApellidoMaterno Nombres". Si el apellido materno
 * es null o vacio, se omite sin dejar espacio colgante.</p>
 */
public record PacienteContextoDTO(
        Long idPersona,
        String nombreCompleto,
        String areaTrabajo,
        String sexo,
        LocalDate fechaNacimiento,
        String celularContacto,
        String correoContacto
) {
}