/**
 * DTOs de respuesta que PersonaService expone a otros módulos.
 *
 * <h3>DTOs de consumo externo (integración entre módulos)</h3>
 * <ul>
 *   <li>{@code PersonaSnapshotClinicoDTO} — Contrato principal con el módulo
 *       Evaluación Nutricional y otros módulos clínicos. Contiene id, edad
 *       calculada al momento de la consulta, sexo ("M"/"F"), área de trabajo
 *       y estado de actividad. El consumidor debe persistir estos valores
 *       como snapshot inmutable.</li>
 *   <li>{@code PersonaResponse} — Respuesta completa de una persona, con
 *       equipo embebido. Uso general para módulos que necesitan ver la ficha
 *       completa.</li>
 *   <li>{@code PersonaResumen} — Vista reducida (id + nombre completo) para
 *       combos y listas.</li>
 *   <li>{@code PersonalAgregado} — Proyección del reporte agregado del módulo
 *       (totales, activos, hombres, mujeres).</li>
 * </ul>
 *
 * <h3>DTOs de uso interno (NO consumir desde fuera)</h3>
 * <ul>
 *   <li>{@code PersonaRequest} — Payload de entrada para crear/actualizar
 *       persona. Solo lo usa PersonaController.</li>
 *   <li>{@code CelularUpdateRequest} — Payload del PATCH parcial de celular.
 *       Solo lo usa PersonaController.</li>
 * </ul>
 *
 * <h3>Nota sobre {@code sexo}</h3>
 * En {@code PersonaSnapshotClinicoDTO}, el campo {@code sexo} es {@code String}
 * ("M" / "F") a propósito: los módulos consumidores no deben importar el enum
 * {@code Sexo} de {@code personal.persona.entity} (paquete interno). Esto
 * desacopla el contrato de integración de los detalles internos de Personal.
 */
@org.springframework.modulith.NamedInterface("persona-dto")
package upeu.edu.pe.Saludabemente.personal.persona.dto;