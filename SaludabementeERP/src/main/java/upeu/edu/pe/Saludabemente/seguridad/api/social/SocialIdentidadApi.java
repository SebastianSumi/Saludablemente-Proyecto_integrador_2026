package upeu.edu.pe.Saludabemente.seguridad.api.social;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Extensión de solo lectura; no cambia SocialUsuarioApi v1.2. */
public interface SocialIdentidadApi {
    /** Cuenta inexistente/inactiva o sin vínculo: empty. */
    Optional<SocialIdentidadDto> obtenerIdentidadActiva(Long idUsuario);

    /** Máximo 100 personas distintas por llamada; omite cuentas ausentes/inactivas. */
    List<SocialIdentidadDto> obtenerPorPersonas(Collection<Long> idsPersona);
}
