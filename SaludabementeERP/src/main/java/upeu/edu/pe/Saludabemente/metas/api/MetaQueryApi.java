package upeu.edu.pe.Saludabemente.metas.api;

import upeu.edu.pe.Saludabemente.metas.api.dto.MetaPublicaDTO;
import upeu.edu.pe.Saludabemente.metas.api.dto.ResumenProgresoPublicDTO;

/**
 * Contrato publico del modulo metas hacia el resto de modulos.
 *
 * Solo consultas de lectura. La gestion (crear meta, registrar check-in,
 * cambiar estado) es responsabilidad del propio modulo metas y no forma
 * parte de este contrato.
 */
public interface MetaQueryApi {

    /**
     * Resumen agregado de las metas de una persona.
     * Si la persona no tiene metas, devuelve el resumen con todo en cero.
     */
    ResumenProgresoPublicDTO resumenPorPersona(Long idPersona);

    /**
     * Indica si la persona tiene al menos una meta CLINICA en estado DESVIADA.
     */
    boolean tieneMetaClinicaDesviada(Long idPersona);

    /**
     * Resumen publico de una meta especifica.
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la meta no existe.
     */
    MetaPublicaDTO obtenerMetaPublica(Long idMeta);
}