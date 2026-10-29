package upeu.edu.pe.Saludabemente.alertaclinica.api;

import upeu.edu.pe.Saludabemente.alertaclinica.api.dto.ResumenAlertaDTO;

/**
 * Contrato de lectura expuesto por el modulo alertaclinica al modulo metas.
 *
 * Permite validar que una alerta existe, pertenece a un colaborador y
 * sigue pendiente, y obtener el resumen necesario para vincular una
 * meta prescrita con su alerta de origen.
 *
 * Solo lectura. Ninguna operacion modifica el estado del sistema.
 */
public interface AlertasMetasApiService {

    /**
     * Verifica que la alerta existe, pertenece a la persona indicada,
     * y sigue en estado PENDIENTE.
     *
     * @throws IllegalArgumentException si idAlerta o idPersona son null
     */
    boolean existeAlertaActiva(Long idAlerta, Long idPersona);

    /**
     * Devuelve el resumen de la alerta, incluidos todos los indicadores
     * alterados que la componen.
     *
     * @throws IllegalArgumentException si idAlerta es null
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la alerta no existe
     */
    ResumenAlertaDTO obtenerResumen(Long idAlerta);
}