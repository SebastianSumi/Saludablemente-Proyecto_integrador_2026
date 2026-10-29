package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.post.entity.PostFijado;
import upeu.edu.pe.Saludabemente.social.api.dto.ScopeFijado;
import java.util.*;
public interface PostFijadoRepository extends JpaRepository<PostFijado,Long> {
    List<PostFijado> findByScopeTipoAndScopeIdOrderByFechaFijadoDesc(ScopeFijado tipo,Long id);
    void deleteByIdPostAndScopeTipoAndScopeId(Long post,ScopeFijado tipo,Long id);
}
