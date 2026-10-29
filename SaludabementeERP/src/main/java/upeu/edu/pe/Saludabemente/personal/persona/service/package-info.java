/**
 * API pública del módulo Personal para consulta de personas.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Evaluación Nutricional</b>: {@code obtenerSnapshotClinico(id)} para
 *       aplicar tablas OMRON (% grasa, % masa muscular, % grasa visceral) y
 *       validar que la persona esté activa antes de registrar la evaluación.</li>
 *   <li><b>Aptitud Física</b>: {@code obtener(id)} para validar existencia
 *       antes de registrar las pruebas físicas.</li>
 *   <li><b>Asistencias</b>: {@code obtener(id)} para validar existencia.</li>
 *   <li><b>Alertas Clínicas</b>: {@code obtenerSnapshotClinico(id)} para
 *       evaluar diagnósticos sobre datos demográficos.</li>
 *   <li><b>Recomendaciones IA</b>: {@code obtenerSnapshotClinico(id)} para
 *       enriquecer el prompt con contexto clínico.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code obtenerSnapshotClinico(Long id)} → {@code PersonaSnapshotClinicoDTO}
 *       (id, edad calculada, sexo, área de trabajo, activo).</li>
 *   <li>{@code obtener(Long id)} → {@code PersonaResponse}.</li>
 *   <li>{@code buscar(...)} → {@code List<PersonaResponse>}.</li>
 *   <li>{@code reporte()} → {@code PersonalAgregado}.</li>
 * </ul>
 *
 * <h3>NO exponer (uso interno del módulo Personal)</h3>
 * <ul>
 *   <li>{@code Persona} — entidad JPA.</li>
 *   <li>{@code PersonaRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PersonaMapper}, {@code PersonaServiceImpl} — detalles de implementación.</li>
 *   <li>{@code PersonaRequest}, {@code CelularUpdateRequest} — DTOs de entrada.</li>
 *   <li>{@code buscarOFallar(Long)} — devuelve la entidad, no usable desde fuera.</li>
 * </ul>
 *
 * <h3>Regla de integración</h3>
 * El consumidor <b>nunca</b> debe:
 * <ul>
 *   <li>Inyectar {@code PersonaRepository} directamente.</li>
 *   <li>Guardar la entidad {@code Persona} como {@code @ManyToOne} en su propio módulo.</li>
 *   <li>Recalcular la edad por su cuenta — siempre debe usar {@code edadCalculada}.</li>
 * </ul>
 * Debe guardar el snapshot recibido como campos propios (capa anticorrupción),
 * para preservar el dato histórico al momento de la evaluación.
 */
@org.springframework.modulith.NamedInterface("persona-service")
package upeu.edu.pe.Saludabemente.personal.persona.service;