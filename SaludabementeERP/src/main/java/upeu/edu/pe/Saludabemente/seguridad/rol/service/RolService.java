package upeu.edu.pe.Saludabemente.seguridad.rol.service;

import upeu.edu.pe.Saludabemente.seguridad.rol.dto.response.RolResponse;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;

import java.util.List;
import java.util.Set;

/**
 * Contrato público del subdominio Rol.
 *
 * <p><b>Alcance Sub-fase 1:</b></p>
 * <ul>
 *   <li>{@link #listarTodos()} — catálogo completo, sin filtro fantasma.</li>
 *   <li>{@link #validarRolesExisten(Set)} — valida IDs antes de asignar.</li>
 * </ul>
 *
 * <p><b>Sub-fase 2 agregará:</b></p>
 * <ul>
 *   <li>Filtro fantasma en {@code listarTodos}: si el solicitante no es
 *       ROOT, ocultar el rol ROOT del catálogo.</li>
 * </ul>
 */
public interface RolService {

    /**
     * Lista el catálogo de roles disponibles.
     */
    List<RolResponse> listarTodos();

    /**
     * Verifica que todos los IDs correspondan a roles existentes.
     * Devuelve las entidades para que el llamador las asigne al usuario.
     *
     * @param ids IDs de roles a validar
     * @return entidades Rol resueltas
     * @throws upeu.edu.pe.Saludabemente.exception.BusinessRuleException
     *         si algún ID no existe (roles fantasma)
     */
    Set<Rol> validarRolesExisten(Set<Long> ids);
}