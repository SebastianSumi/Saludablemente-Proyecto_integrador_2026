package upeu.edu.pe.Saludabemente.social.comentario.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.comentario.entity.ComentarioAdjunto;
import java.util.*;
public interface ComentarioAdjuntoRepository extends JpaRepository<ComentarioAdjunto,Long> {
    List<ComentarioAdjunto> findByIdComentarioOrderByOrdenAsc(Long id);
}
