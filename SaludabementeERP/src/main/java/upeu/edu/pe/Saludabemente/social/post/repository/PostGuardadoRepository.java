package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.post.entity.*;
import java.util.*;
public interface PostGuardadoRepository extends JpaRepository<PostGuardado,PostGuardadoId> {
    List<PostGuardado> findByIdUsuario(Long id);
    List<PostGuardado> findByIdUsuarioAndIdPostIn(Long id,Collection<Long> posts);
}
