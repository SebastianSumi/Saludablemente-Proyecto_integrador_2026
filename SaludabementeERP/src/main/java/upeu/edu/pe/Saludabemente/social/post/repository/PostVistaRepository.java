package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.*;
import upeu.edu.pe.Saludabemente.social.post.entity.PostVista;
import java.util.*;
import java.time.LocalDateTime;
public interface PostVistaRepository extends JpaRepository<PostVista,Long> {
    Optional<PostVista> findByIdPostAndIdUsuario(Long post,Long usuario);
    void deleteByFechaUltimaVistaBefore(LocalDateTime corte);
}
