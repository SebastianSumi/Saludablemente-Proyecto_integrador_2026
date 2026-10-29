package upeu.edu.pe.Saludabemente.seguridad.api.noticias;

/**
 * Interfaz pública de Seguridad consumida por el módulo Noticias.
 *
 * <p><b>Solo lectura.</b> Los 5 métodos de esta interfaz no modifican
 * estado, no devuelven entidades JPA, y <b>lanzan excepciones tipadas</b>
 * cuando el dato no existe (a diferencia de un contrato con
 * {@code Optional}). Esa decisión es deliberada: Noticias pidió
 * comportamiento explícito ante casos negativos.</p>
 *
 * <p><b>Sin JWT todavía:</b> la comunicación es por inyección de
 * dependencias. Cuando llegue JWT, se agregan métodos de sesión como
 * {@code obtenerUsuarioActual()} y {@code tieneRol(...)} sin romper
 * este contrato.</p>
 */
public interface NoticiasUsuarioApi {

    /**
     * Valida que un usuario exista y esté activo para operar como autor
     * de noticia.
     *
     * @param idUsuario PK del usuario a validar
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si el usuario no existe
     * @throws upeu.edu.pe.Saludabemente.exception.BusinessRuleException
     *         si el usuario existe pero está inactivo
     */
    void validarAutorActivo(Long idUsuario);

    /**
     * Obtiene el snapshot de un usuario por su ID.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no existe
     */
    UsuarioNoticiasDTO obtenerPorId(Long idUsuario);

    /**
     * Obtiene el snapshot de un usuario por su email.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no existe o el email es nulo/vacío
     */
    UsuarioNoticiasDTO obtenerPorEmail(String email);

    /**
     * Obtiene el usuario vinculado a una persona (relación 1:1 lógica).
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no hay vínculo para esa persona
     */
    UsuarioNoticiasDTO obtenerPorIdPersona(Long idPersona);

    /**
     * Obtiene solo el nombre completo del usuario.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no existe
     */
    String obtenerNombrePorId(Long idUsuario);
}