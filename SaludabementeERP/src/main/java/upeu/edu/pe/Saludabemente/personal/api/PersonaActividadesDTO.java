package upeu.edu.pe.Saludabemente.personal.api;

/**
 * Snapshot de persona expuesto exclusivamente al modulo Actividades
 * para el subdominio de inscripcion a actividades.
 *
 * <p>Este DTO es paralelo a {@code PersonalSnapshotDTO} (usado por
 * Asistencias). Existen por separado a proposito: cada consumidor tiene
 * su contrato con los campos que necesita, sin acoplarse a los cambios
 * del otro.</p>
 *
 * <p><b>Sobre {@code nombreCompleto}</b>: se calcula en Personal con el
 * formato "Nombres ApellidoPaterno ApellidoMaterno". Si el apellido
 * materno es null o vacio, se omite sin dejar espacio colgante.</p>
 *
 * <p><b>Sobre {@code sexo}</b>: se expone como {@link String} ("M" / "F"),
 * no como el enum {@code Sexo} de {@code personal.persona.entity} (paquete
 * interno no expuesto).</p>
 */
public record PersonaActividadesDTO(
        Long idPersona,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        String areaTrabajo,
        String celular,
        String sexo,
        Boolean activo
) {
}