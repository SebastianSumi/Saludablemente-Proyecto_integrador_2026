package upeu.edu.pe.Saludabemente.personal.persona.dto;

/**
 * Snapshot clínico de una persona, expuesto como contrato de integración
 * entre el módulo Personal y los módulos que aplican reglas clínicas
 * (principalmente Evaluación Nutricional).
 *
 * <p>Captura el estado de la persona <b>en el instante de la consulta</b>.
 * El módulo consumidor DEBE persistir estos valores como campos propios
 * (capa anticorrupción). Así se preserva el dato histórico: si la persona
 * cumple años o cambia de área la semana siguiente, la evaluación conserva
 * los valores exactos del día en que se registró.</p>
 *
 * <p><b>Nomenclatura alineada al consumidor:</b> los nombres de los campos
 * ({@code documentoIdentidad}, {@code nombresCompletos}, {@code edadAlMomento},
 * {@code isActivo}) coinciden con el contrato formalizado por el módulo
 * Evaluación Nutricional. No renombrar sin coordinar con ese equipo.</p>
 *
 * <p><b>Sobre {@code sexo}:</b> se expone como {@link String} ("M" / "F"),
 * NO como el enum {@code Sexo} de {@code personal.persona.entity}. El
 * paquete {@code entity} es interno y no está expuesto vía
 * {@code @NamedInterface}; el String desacopla el contrato de los detalles
 * internos de Personal.</p>
 */
public record PersonaSnapshotClinicoDTO(
        Long idPersona,
        String documentoIdentidad,
        String nombresCompletos,
        String celular,
        Integer edadAlMomento,
        String sexo,
        String areaTrabajo,
        boolean isActivo
) {
}