package upeu.edu.pe.Saludabemente.social.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.post.entity.PostImagen;
import java.util.*;
public interface PostImagenRepository extends JpaRepository<PostImagen,Long> {
    List<PostImagen> findByIdPostOrderByOrdenAsc(Long id);
    void deleteByIdPost(Long id);
}
