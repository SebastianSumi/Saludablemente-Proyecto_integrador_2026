package upeu.edu.pe.Saludabemente.social.reaccion.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.reaccion.entity.*;
import java.util.*;
public interface ReaccionPostRepository extends JpaRepository<ReaccionPost,ReaccionPostId> {
    List<ReaccionPost> findByIdPost(Long id); List<ReaccionPost> findByIdUsuario(Long id);
    List<ReaccionPost> findByIdPostIn(Collection<Long> ids);
    List<ReaccionPost> findByIdPostInAndIdUsuario(Collection<Long> ids,Long usuario);
    @org.springframework.data.jpa.repository.Query("select r.idPost,r.tipo,count(r) from ReaccionPost r where r.idPost in :ids group by r.idPost,r.tipo")
    List<Object[]> contarBatch(@org.springframework.data.repository.query.Param("ids") Collection<Long> ids);
    Page<ReaccionPost> findByIdPostAndTipo(Long id, upeu.edu.pe.Saludabemente.social.api.dto.TipoReaccion tipo, org.springframework.data.domain.Pageable page);
    Page<ReaccionPost> findByIdPost(Long id,org.springframework.data.domain.Pageable page);
}
