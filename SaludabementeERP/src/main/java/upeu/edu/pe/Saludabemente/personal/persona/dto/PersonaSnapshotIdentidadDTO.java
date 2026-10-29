package upeu.edu.pe.Saludabemente.personal.persona.dto;

import java.time.LocalDateTime;

/**
 * Snapshot inmutable de identidad de una persona, expuesto como contrato de
 * integracion entre el modulo Personal y consumidores externos que solo
 * necesitan datos minimos de identificacion.
 *
 * <h3>Para quien</h3>
 * <ul>
 *   <li><b>Seguridad y Usuarios</b>: al dar de alta una cuenta, obtiene este
 *       snapshot para persistirlo en su tabla {@code SLB_SEGURIDAD.USUARIO}
 *       (capa anticorrupcion). Personal no vuelve a ser consultado para ese
 *       {@code idPersona} durante el ciclo de vida del usuario.</li>
 * </ul>
 *
 * <h3>Por que {@code areaTrabajo} es {@code String}</h3>
 * Para evitar que el consumidor importe {@code catalogo.area.dto.AreaResumen}
 * y genere una dependencia cruzada {@code seguridad -> catalogo}. El snapshot
 * captura solo el nombre del area como texto plano; si el area cambia de
 * nombre despues, el snapshot conserva el valor original.
 *
 * <h3>Por que NO tiene {@code sexo}</h3>
 * Para evitar que el consumidor importe {@code personal.persona.entity.Sexo}
 * (paquete interno del modulo Personal, no expuesto via @NamedInterface).
 *
 * <h3>Regla de uso</h3>
 * El consumidor DEBE persistir estos valores como campos propios. No se
 * vuelve a consultar a Personal por el mismo {@code idPersona} salvo
 * operacion explicita de refresh.
 */
public record PersonaSnapshotIdentidadDTO(
        Long idPersona,
        String documentoIdentidad,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String areaTrabajo,
        String nombreEquipo,          // ← NUEVO (nullable)
        boolean activoPersonaSnapshot,
        LocalDateTime snapshotFecha
) {
}