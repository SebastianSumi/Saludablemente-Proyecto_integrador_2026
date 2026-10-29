/**
 * API publica del modulo Catalogo para consulta de periodos.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Evaluacion Nutricional</b>: {@code obtenerVigente()} para saber
 *       en que campana se esta trabajando; {@code obtenerPorCodigo(codigo)}
 *       para resolver el periodo en importaciones masivas desde Excel.</li>
 *   <li><b>Aptitud Fisica</b>: mismo uso que Nutricional.</li>
 * </ul>
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code obtenerVigente()} → {@code PeriodoResponse} con el periodo
 *       actualmente activo.</li>
 *   <li>{@code obtenerPorCodigo(String)} → {@code PeriodoResponse}
 *       buscado por codigo estandarizado (ej. "2026-1").</li>
 *   <li>{@code listarResumen()} → {@code List<PeriodoResumen>}.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>{@code PeriodoRepository} — acceso a datos (regla S3).</li>
 *   <li>{@code Periodo} — entidad JPA.</li>
 *   <li>Metodos de escritura — solo los usa {@code PeriodoController}.</li>
 * </ul>
 *
 * <h3>Regla de integracion</h3>
 * El consumidor NUNCA decide por su cuenta si un periodo esta abierto o
 * cerrado. Siempre consulta el estado via este servicio.
 */
@org.springframework.modulith.NamedInterface("periodo-service")
package upeu.edu.pe.Saludabemente.catalogo.periodo.service;