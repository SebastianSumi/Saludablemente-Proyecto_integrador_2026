package upeu.edu.pe.Saludabemente.social.reaccion.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.reaccion.entity.*;
import java.util.*;
public interface ReaccionComentarioRepository extends JpaRepository<ReaccionComentario,ReaccionComentarioId> {

    List<ReaccionComentario> findByIdComentario(Long id);
    Page<ReaccionComentario> findByIdComentario(Long id, org.springframework.data.domain.Pageable page);
    Page<ReaccionComentario> findByIdComentarioAndTipo(Long id,upeu.edu.pe.Saludabemente.social.api.dto.TipoReaccion tipo,org.springframework.data.domain.Pageable page);
}
