package upeu.edu.pe.Saludabemente.social.comentario.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.comentario.entity.ComentarioHashtag;
import java.util.*;
public interface ComentarioHashtagRepository extends JpaRepository<ComentarioHashtag,Long> {
    List<ComentarioHashtag> findByIdComentario(Long id); void deleteByIdComentario(Long id);
    @org.springframework.data.jpa.repository.Query("select h.tag,count(h) from ComentarioHashtag h, Comentario c,Post p where c.idComentario=h.idComentario and c.activo=true and p.idPost=c.idPost and p.activo=true and c.fechaComentario>=:desde and c.fechaComentario<=:hasta and (p.origenPostId is null or exists(select o from Post o where o.idPost=p.origenPostId and o.activo=true)) group by h.tag")
    List<Object[]> contarRango(@org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,@org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);
    @org.springframework.data.jpa.repository.Query("select distinct h.tag from ComentarioHashtag h,Comentario c,Post p where c.idComentario=h.idComentario and c.activo=true and p.idPost=c.idPost and p.activo=true and h.tag like :patron and (p.origenPostId is null or exists(select o from Post o where o.idPost=p.origenPostId and o.activo=true))")
    List<String> buscarTags(@org.springframework.data.repository.query.Param("patron") String patron);
}
