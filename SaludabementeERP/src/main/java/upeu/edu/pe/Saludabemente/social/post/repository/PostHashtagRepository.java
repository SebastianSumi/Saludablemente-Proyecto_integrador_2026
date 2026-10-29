package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.post.entity.PostHashtag;
import java.util.*;
public interface PostHashtagRepository extends JpaRepository<PostHashtag,Long> {
    List<PostHashtag> findByIdPost(Long id); void deleteByIdPost(Long id);
    @org.springframework.data.jpa.repository.Query("select h.tag,count(h) from PostHashtag h, Post p where p.idPost=h.idPost and p.activo=true and p.fechaPublicacion>=:desde and p.fechaPublicacion<=:hasta and (p.origenPostId is null or exists(select o from Post o where o.idPost=p.origenPostId and o.activo=true)) group by h.tag")
    List<Object[]> contarRango(@org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,@org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);
    @org.springframework.data.jpa.repository.Query("select distinct h.tag from PostHashtag h,Post p where p.idPost=h.idPost and p.activo=true and h.tag like :patron and (p.origenPostId is null or exists(select o from Post o where o.idPost=p.origenPostId and o.activo=true))")
    List<String> buscarTags(@org.springframework.data.repository.query.Param("patron") String patron);
}
