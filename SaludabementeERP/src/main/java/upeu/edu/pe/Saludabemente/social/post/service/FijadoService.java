package upeu.edu.pe.Saludabemente.social.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import upeu.edu.pe.Saludabemente.social.post.repository.*;
import upeu.edu.pe.Saludabemente.social.post.dto.FijarRequest;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.seguridad.api.social.SocialUsuarioActualDto;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class FijadoService {
    private final PostRepository posts;
    private final PostFijadoRepository pins;
    private final SocialAccesoService acceso;
    private final Clock socialClock;
    @org.springframework.beans.factory.annotation.Value("${saludablemente.social.noticias-oficiales.auto-pin-dias:7}")
    private int autoPinDias=7;
    public List<PostFijado> activos(ScopeFijado tipo,Long scope) {
        return pins.findByScopeTipoAndScopeIdOrderByFechaFijadoDesc(tipo,scope).stream()
                .filter(p -> p.getFechaExpiracion()==null || p.getFechaExpiracion().isAfter(LocalDateTime.now(socialClock))).toList();
    }
    public void fijar(Long id,FijarRequest r,SocialUsuarioActualDto u) {
        acceso.admin(u);validar(r.scopeTipo(),r.scopeId());
        posts.findFirstByOrderByIdPostAsc();Post p=acceso.visible(id);
        if(r.fechaExpiracion()!=null && !r.fechaExpiracion().isAfter(LocalDateTime.now(socialClock))) throw new BusinessRuleException("Expiración debe ser futura");
        var actuales=activos(r.scopeTipo(),r.scopeId());
        if(actuales.stream().anyMatch(f -> f.getIdPost().equals(id))) throw new BusinessRuleException("Ya está fijado");
        if(actuales.size()>=3) throw new BusinessRuleException("Máximo tres fijados por scope");
        pins.deleteByIdPostAndScopeTipoAndScopeId(id,r.scopeTipo(),r.scopeId());pins.flush();
        crear(p,r.scopeTipo(),r.scopeId(),r.fechaExpiracion(),u.idUsuario());
    }
    public void quitar(Long id,ScopeFijado tipo,Long scope,SocialUsuarioActualDto u) {
        acceso.admin(u);validar(tipo,scope);posts.findFirstByOrderByIdPostAsc();pins.deleteByIdPostAndScopeTipoAndScopeId(id,tipo,scope);
    }
    public void autoFijar(Post p) {
        posts.findFirstByOrderByIdPostAsc();
        var actuales=activos(ScopeFijado.GLOBAL,null);
        if(actuales.size()>=3 || actuales.stream().anyMatch(f -> f.getIdPost().equals(p.getIdPost()))) return;
        // Un pin expirado del mismo post no se renueva por una sincronización repetida.
        if(pins.findByScopeTipoAndScopeIdOrderByFechaFijadoDesc(ScopeFijado.GLOBAL,null).stream().anyMatch(f -> f.getIdPost().equals(p.getIdPost()))) return;
        if(autoPinDias>0) crear(p,ScopeFijado.GLOBAL,null,LocalDateTime.now(socialClock).plusDays(autoPinDias),p.getAutorId());
    }
    private void crear(Post p,ScopeFijado tipo,Long scope,LocalDateTime expiracion,Long usuario) {
        PostFijado f=new PostFijado();f.setIdPost(p.getIdPost());f.setScopeTipo(tipo);f.setScopeId(scope);
        f.setFechaFijado(LocalDateTime.now(socialClock));f.setFechaExpiracion(expiracion);f.setIdUsuarioFijador(usuario);pins.save(f);
    }
    private void validar(ScopeFijado tipo,Long scope) {
        if(tipo==null || tipo==ScopeFijado.GLOBAL && scope!=null || tipo!=ScopeFijado.GLOBAL && (scope==null || scope<=0))
            throw new BusinessRuleException("Scope inválido");
    }
}
