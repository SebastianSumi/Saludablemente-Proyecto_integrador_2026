package upeu.edu.pe.Saludabemente.personal.api;

import java.time.LocalDate;

/**
 * Snapshot de una persona para consumo externo por parte de otros modulos
 * (Asistencias, Seguridad, Reportes).
 *
 * <p>El consumidor DEBE persistir los valores como campos propios. No se
 * vuelve a consultar a Personal por el mismo {@code idPersona} salvo
 * operacion explicita de refresh.</p>
 *
 * <p><b>Sobre {@code sexo}</b>: se expone como {@link Character} ('M' / 'F'),
 * no como el enum {@code Sexo} de {@code personal.persona.entity} (paquete
 * interno no expuesto).</p>
 */
public record PersonalSnapshotDTO(
        Long idPersona,
        String nombreCompleto,
        Boolean activo,
        Long idTeam,
        String nombreTeam,
        String areaTrabajo,
        String email,
        String celular,
        Character sexo,
        LocalDate fechaNacimiento
) {
}