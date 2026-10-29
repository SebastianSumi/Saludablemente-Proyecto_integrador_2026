package upeu.edu.pe.Saludabemente.personal.api;

import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;

import java.util.List;
import java.util.Optional;

/**
 * API publica del modulo Personal para consultar personas desde otros
 * modulos. Contrato de solo lectura.
 *
 * <p>Todos los metodos son seguros: nunca lanzan excepcion por persona
 * inexistente. Devuelven {@code null} o listas vacias segun corresponda.</p>
 */
public interface PersonalApiService {

    /**
     * Valida existencia y estado de una persona.
     * <ul>
     *   <li>Si no existe: devuelve {@code null}.</li>
     *   <li>Si existe y esta activa: devuelve snapshot con {@code activo = true}.</li>
     *   <li>Si existe pero esta inactiva: devuelve snapshot con {@code activo = false}.</li>
     * </ul>
     */
    PersonalSnapshotDTO validarExistencia(Long idPersona);

    /**
     * Valida existencia en lote. Solo devuelve las personas que existen
     * Y estan activas. Las que no existen o estan inactivas no vienen en
     * la lista.
     * <p>Si {@code ids} es {@code null} o vacio, devuelve lista vacia.</p>
     */
    List<PersonalSnapshotDTO> validarExistencias(List<Long> ids);

    /**
     * Lista todos los colaboradores activos con datos completos.
     * Ordenados por apellido paterno. Volumen esperado: 100-5000.
     */
    List<PersonalSnapshotDTO> listarPersonalActivo();

    /**
     * Snapshot individual de una persona, exista o no este activa.
     * Si no existe: {@code null}.
     */
    PersonalSnapshotDTO obtenerSnapshot(Long idPersona);

    /**
     * Snapshots en lote. Devuelve todos los que existan, activos o inactivos.
     * Si {@code ids} es {@code null} o vacio, devuelve lista vacia.
     */
    List<PersonalSnapshotDTO> obtenerSnapshots(List<Long> ids);

    /**
     * Lista los colaboradores activos de un equipo especifico.
     * Si el equipo no existe o no tiene miembros activos, devuelve lista vacia.
     */
    List<PersonalSnapshotDTO> listarPorTeam(Long idTeam);

    /**
     * Cuenta cuantas personas activas tiene un equipo.
     * Devuelve 0 si el equipo no existe o no tiene miembros activos.
     */
    int contarPersonasActivas(Long idTeam);

    /**
     * Devuelve el snapshot de identidad de todas las personas activas,
     * ordenadas por apellido paterno.
     *
     * <p>Uso: modulo Seguridad y Usuarios, para sincronizacion masiva
     * o validacion batch de cuentas.</p>
     *
     * <p>Nunca devuelve {@code null}. Si no hay personas activas, devuelve
     * lista vacia.</p>
     */
    List<PersonaSnapshotIdentidadDTO> listarSnapshotsIdentidadActivos();

    PersonaSnapshotIdentidadDTO obtenerSnapshotIdentidad(Long idPersona);

    /**
     * Devuelve el resumen de un area por su id.
     * Si no existe, devuelve vacio.
     */
}