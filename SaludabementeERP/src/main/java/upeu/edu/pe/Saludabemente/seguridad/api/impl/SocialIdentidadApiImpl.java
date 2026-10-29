package upeu.edu.pe.Saludabemente.seguridad.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialIdentidadDto;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialIdentidadApiImpl implements SocialIdentidadApi {
    private final UsuarioRepository usuarios;

    @Override
    public Optional<SocialIdentidadDto> obtenerIdentidadActiva(Long idUsuario) {
        if (idUsuario == null) return Optional.empty();
        return usuarios.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()) && u.getIdPersonaSnapshot() != null)
                .map(this::dto);
    }

    @Override
    public List<SocialIdentidadDto> obtenerPorPersonas(Collection<Long> idsPersona) {
        if (idsPersona == null || idsPersona.isEmpty()) return List.of();
        List<Long> ids = idsPersona.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.size() > 100) throw new BusinessRuleException("Máximo 100 personas por lote");
        if (ids.isEmpty()) return List.of();
        return usuarios.findByActivoTrueAndIdPersonaSnapshotIn(ids).stream().map(this::dto).toList();
    }

    private SocialIdentidadDto dto(Usuario u) {
        return new SocialIdentidadDto(u.getId(), u.getIdPersonaSnapshot(), u.getEmail(),
                u.getRoles().stream().map(r -> r.getNombre()).sorted().toList());
    }
}
