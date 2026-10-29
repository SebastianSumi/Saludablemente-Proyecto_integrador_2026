package upeu.edu.pe.Saludabemente.social.integracion.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.seguridad.api.social.*;
import java.util.*;

/** Adaptador de identidad: batch sin snapshots persistidos ni dependencia de proveedores externos. */
@Service
@RequiredArgsConstructor
public class SocialUsuarioClientService {
    private final SocialUsuarioApi usuarios;

    public SocialUsuarioActualDto obtenerUsuarioActual() {
        try { return usuarios.obtenerUsuarioActual(); }
        catch (UsuarioSocialInactivoException inactivo) {
            throw new BusinessRuleException("La cuenta del usuario está inactiva");
        }
    }

    /** Mapa por ID, incluyendo placeholder para cada ID solicitado no resuelto. */
    public Map<Long, SocialUsuarioSnapshotDto> autores(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        List<Long> unicos = ids.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, SocialUsuarioSnapshotDto> resultado = new LinkedHashMap<>();
        for (Long id : unicos) resultado.put(id, placeholder(id));
        for (int i = 0; i < unicos.size(); i += 100) {
            for (SocialUsuarioSnapshotDto u : usuarios.obtenerPorIds(unicos.subList(i, Math.min(i + 100, unicos.size())))) {
                if (resultado.containsKey(u.idUsuario())) resultado.put(u.idUsuario(), u);
            }
        }
        return Collections.unmodifiableMap(resultado);
    }

    private SocialUsuarioSnapshotDto placeholder(Long id) {
        return new SocialUsuarioSnapshotDto(id, "eliminado_" + id, null, "[Usuario eliminado]",
                null, List.of(), null, null, false);
    }
}
