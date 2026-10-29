package upeu.edu.pe.Saludabemente.seguridad.api.metas;

/**
 * Interfaz pública de Seguridad consumida por el módulo Metas.
 *
 * <p>Resuelve quién ejecuta la petición y qué roles tiene, sin depender
 * de JWT ni de {@code @PreAuthorize}. Cuando JWT llegue, esta interfaz
 * no cambia — solo su implementación interna.</p>
 */
public interface MetasUsuarioApi {

    /**
     * Devuelve el usuario que ejecuta la petición HTTP actual.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si no se puede resolver el usuario actual
     */
    UsuarioActualDTO obtenerUsuarioActual();

    /**
     * Verifica si el usuario actual tiene el rol indicado.
     *
     * @param codigoRol código de rol del vocabulario cerrado
     * @throws IllegalArgumentException si {@code codigoRol} es nulo o vacío
     */
    boolean tieneRol(String codigoRol);

    /**
     * Verifica si el usuario actual tiene al menos uno de los roles indicados.
     *
     * @param codigosRol uno o más códigos de rol
     * @throws IllegalArgumentException si {@code codigosRol} es nulo, vacío,
     *         o contiene valores nulos o vacíos
     */
    boolean tieneAlgunRol(String... codigosRol);
}