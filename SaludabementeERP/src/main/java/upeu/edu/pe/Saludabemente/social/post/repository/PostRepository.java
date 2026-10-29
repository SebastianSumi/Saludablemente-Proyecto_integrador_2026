package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import upeu.edu.pe.Saludabemente.social.post.entity.Post;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoPost;
import java.util.*;
public interface PostRepository extends JpaRepository<Post,Long>, JpaSpecificationExecutor<Post> {
    Optional<Post> findByTipoContenidoAndRefId(TipoContenidoPost tipo,Long refId);
    Optional<Post> findByAutorIdAndOrigenPostId(Long autor,Long origen);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Post p where p.idPost=:id")
    Optional<Post> bloquear(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<Post> findFirstByOrderByIdPostAsc();
    long countByOrigenPostIdAndActivoTrue(Long origen);
}
