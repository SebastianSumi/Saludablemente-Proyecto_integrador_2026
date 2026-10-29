package upeu.edu.pe.Saludabemente.metas.api;

import upeu.edu.pe.Saludabemente.metas.api.dto.ResultadoActualizacionDTO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Contrato publico de escritura del modulo metas.
 *
 * Consumido por nutricional despues de persistir una evaluacion COMPLETA
 * (antropometrico + bioquimico). Sincroniza el valorActual de las metas
 * ACTIVAS de la persona cuyo indicador coincida con los medidos.
 *
 * Reglas:
 *  - Solo actualiza metas ACTIVAS con tipoCalculo = ULTIMO_VALOR.
 *  - Idempotente: repetir con los mismos datos no produce cambios.
 *  - Si tras la actualizacion el avance es >= 100%, la meta se marca COMPLETADA.
 *  - Claves de indicadores: vocabulario cerrado de 12 entradas.
 */
public interface MetaActualizacionApi {

    ResultadoActualizacionDTO actualizarDesdeEvaluacion(
            Long idPersona,
            Long idEvaluacion,
            Map<String, BigDecimal> indicadoresActualizados);
}