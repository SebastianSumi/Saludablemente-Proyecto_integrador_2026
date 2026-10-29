package upeu.edu.pe.Saludabemente.catalogo.area.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<AreaTrabajo, Long> {

    List<AreaTrabajo> findByActivoTrue(Sort sort);

    List<AreaTrabajo> findByActivoFalse(Sort sort);

    Optional<AreaTrabajo> findByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrue(String nombre);
}