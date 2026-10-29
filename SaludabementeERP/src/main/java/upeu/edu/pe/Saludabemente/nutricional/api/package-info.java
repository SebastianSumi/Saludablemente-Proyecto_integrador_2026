/**
 * API pública del módulo Nutricional.
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Alertas Clínicas</b>: recibe {@code EvaluacionNutricionalIngresoDto}
 *       cuando una evaluación pasa a COMPLETA.</li>
 *   <li><b>Metas</b>: consulta contexto clínico para inicializar metas.</li>
 *   <li><b>Recomendaciones IA</b>: consulta cabecera, antropometría,
 *       bioquímica y evaluación anterior.</li>
 * </ul>
 *
 * <h3>Contratos expuestos</h3>
 * <ul>
 *   <li>{@code EvaluacionApiService} — consulta genérica del historial.</li>
 *   <li>{@code EvaluacionMetasApiService} — contrato con Metas.</li>
 *   <li>{@code RecomendacionesNutricionalApiService} — contrato con Recomendaciones IA.</li>
 *   <li>{@code EvaluacionNutricionalDto}, {@code AntropometricoDto},
 *       {@code BioquimicoDto}, {@code EvaluacionHistoricaDto} — DTOs
 *       específicos para Recomendaciones IA.</li>
 *   <li>{@code ContextoClinicoDTO} — contexto clínico para Metas.</li>
 *   <li>{@code EvaluacionResumenDTO} — vista reducida genérica.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>El subpaquete {@code api.impl}.</li>
 *   <li>La entidad {@code EvaluacionNutricional}.</li>
 *   <li>Los repositorios internos.</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("nutricional-api")
package upeu.edu.pe.Saludabemente.nutricional.api;