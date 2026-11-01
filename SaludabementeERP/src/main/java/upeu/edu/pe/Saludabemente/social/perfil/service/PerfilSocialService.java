package upeu.edu.pe.Saludabemente.social.perfil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.personal.api.social.SocialEquipoApi;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioApi;
import upeu.edu.pe.Saludabemente.social.integracion.client.SocialUsuarioClientService;
import upeu.edu.pe.Saludabemente.social.perfil.dto.PerfilCabeceraResponse;
import upeu.edu.pe.Saludabemente.social.post.dto.AutorResponse;
import upeu.edu.pe.Saludabemente.social.post.service.SocialConsultaService;

import java.util.ArrayList;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerfilSocialService {

    private final SocialUsuarioApi usuarios;
    private final SocialUsuarioClientService autores;
    private final SocialEquipoApi equipos;
    private final SocialConsultaService consulta;

    public PerfilCabeceraResponse cabecera(Long id, SocialUsuarioActualDto actor) {
        var visual = usuarios.obtenerPerfilVisual(id);
        var autor = consulta.autor(id);
        autor = new AutorResponse(autor.idUsuario(), autor.username(), autor.nombreCompleto(),
                autor.rolPrincipal(), autor.nombreEquipo(), autor.idEquipo(), visual.fotoUrl());
        return new PerfilCabeceraResponse(autor, visual.portadaUrl(), visual.presentacion(),
                id.equals(actor.idUsuario()), equipos.equiposDeUsuario(id));
    }

    public Page<AutorResponse> companeros(Long id, Pageable page) {
        usuarios.obtenerPorId(id);
        var ids = new TreeSet<Long>();
        for (var equipo : equipos.equiposDeUsuario(id)) {
            ids.addAll(equipos.miembrosActivosDeEquipo(equipo.idEquipo()));
        }
        ids.remove(id);
        var lista = new ArrayList<>(ids);
        int desde = (int) Math.min(page.getOffset(), lista.size());
        var pagina = lista.subList(desde, Math.min(desde + page.getPageSize(), lista.size()));
        var mapa = autores.autores(pagina);
        return new PageImpl<>(pagina.stream().map(mapa::get).map(consulta::autor).toList(), page, lista.size());
    }

    @Transactional
    public PerfilCabeceraResponse editar(String presentacion, SocialUsuarioActualDto actor) {
        usuarios.actualizarMiPresentacion(presentacion);
        return cabecera(actor.idUsuario(), actor);
    }

    @Transactional
    public PerfilCabeceraResponse foto(MultipartFile archivo, boolean portada, SocialUsuarioActualDto actor) {
        usuarios.subirMiFoto(archivo, portada);
        return cabecera(actor.idUsuario(), actor);
    }
}
