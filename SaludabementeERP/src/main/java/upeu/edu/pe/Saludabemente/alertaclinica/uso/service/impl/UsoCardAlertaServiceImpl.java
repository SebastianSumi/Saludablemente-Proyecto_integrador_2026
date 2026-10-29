package upeu.edu.pe.Saludabemente.alertaclinica.uso.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.entity.UsoCardAlerta;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.repository.UsoCardAlertaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.service.UsoCardAlertaService;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.util.CurrentUserProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsoCardAlertaServiceImpl implements UsoCardAlertaService {

    private final UsoCardAlertaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> obtener() {
        List<UsoCardAlerta> usos = repository.findByIdUsuario(idUsuarioActual());
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (UsoCardAlerta uso : usos) {
            mapa.put(uso.getRuta(), uso.getConteo());
        }
        return mapa;
    }

    @Override
    @Transactional
    public void registrar(String ruta) {
        Long idUsuario = idUsuarioActual();
        UsoCardAlerta uso = repository.findByIdUsuarioAndRuta(idUsuario, ruta)
                .orElseGet(() -> {
                    UsoCardAlerta nuevo = new UsoCardAlerta();
                    nuevo.setIdUsuario(idUsuario);
                    nuevo.setRuta(ruta);
                    nuevo.setConteo(0);
                    return nuevo;
                });
        uso.setConteo(uso.getConteo() + 1);
        repository.save(uso);
    }

    @Override
    @Transactional
    public void reiniciar() {
        repository.deleteByIdUsuario(idUsuarioActual());
    }

    /** Resuelve el id del usuario autenticado desde el email del JWT. */
    private Long idUsuarioActual() {
        String email = currentUserProvider.obtenerEmailDesdeRequest()
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return usuario.getId();
    }
}
