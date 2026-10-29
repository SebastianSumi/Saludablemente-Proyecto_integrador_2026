package upeu.edu.pe.Saludabemente.personal.api;

import java.time.LocalDate;

/**
 * Datos basicos de una persona, expuestos exclusivamente al modulo
 * Notificaciones para contextualizar envios y personalizar mensajes.
 *
 * <p><b>Sobre {@code sexo}</b>: se expone como {@link String} ("M" / "F"),
 * no como el enum {@code Sexo} del paquete {@code personal.persona.entity}
 * (interno, no expuesto).</p>
 *
 * <p><b>Sobre {@code apellidos}</b>: se concatena "ApellidoPaterno
 * ApellidoMaterno". Si no hay apellido materno, se omite sin espacio
 * doble.</p>
 *
 * <p><b>Sobre {@code idTeam}</b>: puede ser {@code null} si la persona
 * no esta asignada a un team.</p>
 */
public record DatosBasicosPersonaDTO(
        Long idPersona,
        String nombres,
        String apellidos,
        String sexo,
        LocalDate fechaNacimiento,
        Long idTeam
) {
}