/**
 * Contrato publico del modulo Catalogo / Periodo.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Evaluacion Nutricional</b>: usa {@code PeriodoResponse} y
 *       {@code EstadoPeriodo} para validar que un periodo no este CERRADO
 *       antes de crear o importar evaluaciones.</li>
 *   <li><b>Aptitud Fisica</b>: mismo uso que Nutricional.</li>
 *   <li><b>Actividades</b>: agrupa actividades por periodo.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code PeriodoResponse} — datos completos del periodo.</li>
 *   <li>{@code PeriodoResumen} — vista reducida.</li>
 *   <li>{@code EstadoPeriodo} — enum con los 3 estados posibles.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code Periodo} — entidad JPA.</li>
 *   <li>{@code PeriodoRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code PeriodoMapper}, {@code PeriodoServiceImpl} — detalles internos.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("periodo-dto")
package upeu.edu.pe.Saludabemente.catalogo.periodo.dto;