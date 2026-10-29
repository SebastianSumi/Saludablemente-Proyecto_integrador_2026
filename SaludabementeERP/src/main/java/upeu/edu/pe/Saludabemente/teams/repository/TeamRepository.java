package upeu.edu.pe.Saludabemente.teams.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByActivoTrue(Sort sort);

    Optional<Team> findByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrue(String nombre);

    List<Team> findByActivoFalse(Sort sort);
}
