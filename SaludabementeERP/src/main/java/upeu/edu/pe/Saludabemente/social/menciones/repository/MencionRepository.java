package upeu.edu.pe.Saludabemente.social.menciones.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.social.menciones.entity.Mencion;
import upeu.edu.pe.Saludabemente.social.api.dto.TipoContenidoMencion;
import java.util.*;
public interface MencionRepository extends JpaRepository<Mencion,Long> {
    List<Mencion> findByTipoContenidoAndIdContenido(TipoContenidoMencion tipo,Long id);
    void deleteByTipoContenidoAndIdContenido(TipoContenidoMencion tipo,Long id);
}
