package upeu.edu.pe.Saludabemente.catalogo.periodo.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.EstadoPeriodo;
import upeu.edu.pe.Saludabemente.catalogo.periodo.entity.Periodo;

import java.util.List;
import java.util.Optional;

public interface PeriodoRepository extends JpaRepository<Periodo, Long> {

    Optional<Periodo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Periodo> findByEstado(EstadoPeriodo estado);

    List<Periodo> findByEstado(EstadoPeriodo estado, Sort sort);

    Optional<Periodo> findFirstByEstado(EstadoPeriodo estado);
}