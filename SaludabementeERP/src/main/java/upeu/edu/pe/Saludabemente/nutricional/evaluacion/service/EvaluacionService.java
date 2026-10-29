package upeu.edu.pe.Saludabemente.nutricional.evaluacion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.reporte.EvaluacionAgregado;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.request.EvaluacionInitRequest;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResponse;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.dto.response.EvaluacionResumen;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;

import java.util.List;

/**
 * Orquestación del ciclo de vida de la cabecera de evaluación nutricional.
 * Cubre las funciones 7-14 de PLANIFICACION.docx.
 *
 * <p>Este servicio es <b>interno al módulo nutricional</b>. Los subdominios
 * {@code antropometrico} y {@code bioquimico} lo consumen para validar la
 * existencia de la cabecera y disparar la transición a COMPLETA.</p>
 *
 * <p>Consume dos contratos externos:</p>
 * <ul>
 *   <li>{@code PersonaService.obtenerSnapshotClinico(id)} — datos demográficos
 *       congelados al momento de la evaluación.</li>
 *   <li>{@code PeriodoService.obtenerPorCodigo(codigo)} — valida que el periodo
 *       exista y no esté CERRADO antes de abrir la evaluación.</li>
 * </ul>
 *
 * <p>La exposición hacia otros módulos (Alertas Clínicas, Recomendaciones IA)
 * se decide en Fase 9 con un {@code @NamedInterface} explícito.</p>
 */
public interface EvaluacionService {

    // -------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------

    Page<EvaluacionResumen> buscar(EstadoEvaluacion estado,
                                   String periodo,
                                   Long idPersona,
                                   Pageable pageable);

    EvaluacionAgregado agregados(String periodo);

    EvaluacionResponse obtener(Long id);

    List<EvaluacionResumen> historialPorPersona(Long idPersona);

    List<EvaluacionResumen> incompletasPorPeriodo(String periodo);

    // -------------------------------------------------------------------
    // Comandos
    // -------------------------------------------------------------------

    EvaluacionResponse iniciar(EvaluacionInitRequest request);

    EvaluacionResponse actualizarObservaciones(Long id, String observaciones);

    void anular(Long id);

    // -------------------------------------------------------------------
    // API interna para subdominios del propio módulo
    // -------------------------------------------------------------------

    /**
     * Devuelve la cabecera con ambos detalles cargados vía @EntityGraph.
     * Lanza ResourceNotFoundException si no existe.
     */
    EvaluacionNutricional obtenerOFallar(Long id);

    /**
     * Verifica si existen ambos detalles. Si es así, transiciona a COMPLETA
     * y dispara las integraciones (Alertas Clínicas, Recomendaciones IA).
     */
    void evaluarCompletitudYDisparar(Long idEvaluacion);

    List<PeriodoResponse> listarPeriodosDisponibles();
}