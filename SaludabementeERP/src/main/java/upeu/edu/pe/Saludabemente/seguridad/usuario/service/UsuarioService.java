package upeu.edu.pe.Saludabemente.seguridad.usuario.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.PasswordRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RolesRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;

/**
 * Contrato público del subdominio Usuario.
 *
 * <p><b>Alcance Sub-fase 1:</b></p>
 * <ul>
 *   <li>CRUD de cuentas con reglas: persona existe y está activa, 1:1,
 *       email único, mínimo un rol, contraseña cifrada.</li>
 *   <li>Baja lógica y reactivación.</li>
 *   <li>Actualización de roles y contraseña.</li>
 * </ul>
 *
 * <p><b>Sub-fase 2 agregará:</b></p>
 * <ul>
 *   <li>Guard clauses de ROOT en actualizarRoles, actualizarPassword
 *       e inactivar.</li>
 *   <li>Filtro fantasma en {@link #buscar} (excluir ROOT si el
 *       solicitante no es ROOT).</li>
 *   <li>Operación {@code suplantar} (exclusiva ROOT).</li>
 * </ul>
 */
public interface UsuarioService {

    /**
     * Da de alta una cuenta nueva.
     * Captura el snapshot de identidad desde Personal y lo persiste.
     */
    UsuarioResponse crear(UsuarioRequest request);

    /**
     * Búsqueda paginada con filtros dinámicos.
     *
     * @param email   filtro parcial por email (LIKE, case-insensitive). Null = sin filtro
     * @param activo  filtro por estado de cuenta. Null = sin filtro
     * @param pageable paginación y ordenamiento
     */
    Page<UsuarioResponse> buscar(String email, Boolean activo, Pageable pageable);

    /**
     * Perfil 360° de un usuario: credenciales + roles + snapshot identidad.
     */
    UsuarioResponse obtener(Long id);

    /**
     * Reemplaza por completo los roles de un usuario.
     * Valida que al menos uno quede asignado.
     */
    UsuarioResponse actualizarRoles(Long id, RolesRequest request);

    /**
     * Cambia la contraseña (se almacena cifrada).
     */
    void actualizarPassword(Long id, PasswordRequest request);

    /**
     * Baja lógica: cambia activo = false.
     */
    void inactivar(Long id);

    /**
     * Reactiva una cuenta previamente inactivada.
     */
    void reactivar(Long id);

    /**
     * Utilitario interno expuesto para otros servicios del mismo módulo.
     */
    Usuario buscarOFallar(Long id);


}