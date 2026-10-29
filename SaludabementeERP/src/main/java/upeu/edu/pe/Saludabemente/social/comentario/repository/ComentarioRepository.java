package upeu.edu.pe.Saludabemente.social.comentario.repository;
import org.springframework.data.jpa.repository.*;
import upeu.edu.pe.Saludabemente.social.comentario.entity.Comentario;
import java.util.*;
public interface ComentarioRepository extends JpaRepository<Comentario,Long>,JpaSpecificationExecutor<Comentario> {
    List<Comentario> findByIdPostOrderByFechaComentarioAscIdComentarioAsc(Long id);
    List<Comentario> findByIdComentarioPadre(Long id);
    long countByIdPostAndActivoTrue(Long id);
    @Query("select c.idPost,count(c) from Comentario c where c.idPost in :ids and c.activo=true group by c.idPost")
    List<Object[]> contarBatch(@org.springframework.data.repository.query.Param("ids") Collection<Long> ids);
    @Query("select c from Comentario c where c.idPost=:id and c.idComentarioPadre is null and (c.activo=true or exists (select h from Comentario h where h.idComentarioPadre=c.idComentario))")
    org.springframework.data.domain.Page<Comentario> raices(@org.springframework.data.repository.query.Param("id") Long id,org.springframework.data.domain.Pageable page);
    List<Comentario> findByIdComentarioPadreInAndActivoTrueOrderByFechaComentarioAscIdComentarioAsc(Collection<Long> ids);
}
