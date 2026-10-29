/**
 * API pública del módulo Aptitud Física para consumidores externos.
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code AptitudApiService} — consultas públicas</li>
 *   <li>{@code MotorAlertasFisicaPort} — puerto hacia el motor de alertas</li>
 *   <li>{@code EvaluacionFisicaIngresoDto}, {@code MetricaFisicaDto},
 *       {@code TipoMetrica} — payloads de integración</li>
 * </ul>
 *
 * <h3>NO expuesto</h3>
 * Las implementaciones viven en {@code api.impl} (sub-paquete sin
 * {@code @NamedInterface}) o en {@code evaluacion.service.impl}.
 */
@org.springframework.modulith.NamedInterface("aptitud-fisica-api")
package upeu.edu.pe.Saludabemente.aptitudfisica.api;