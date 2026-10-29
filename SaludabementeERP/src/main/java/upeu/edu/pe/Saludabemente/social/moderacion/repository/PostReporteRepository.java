package upeu.edu.pe.Saludabemente.social.moderacion.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import upeu.edu.pe.Saludabemente.social.moderacion.entity.PostReporte;
import upeu.edu.pe.Saludabemente.social.api.dto.EstadoReporte;
public interface PostReporteRepository extends JpaRepository<PostReporte,Long> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select r from PostReporte r where r.idPostReporte=:id")
    java.util.Optional<PostReporte> bloquear(@org.springframework.data.repository.query.Param("id") Long id);
    Page<PostReporte> findByEstadoOrderByFechaReporteAsc(EstadoReporte estado,Pageable page);
    boolean existsByIdUsuarioReportaAndIdPost(Long usuario,Long post);
    boolean existsByIdUsuarioReportaAndIdComentario(Long usuario,Long comentario);
}
