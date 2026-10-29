package upeu.edu.pe.Saludabemente.personal.preferencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.personal.preferencia.entity.PreferenciaComunicacion;

import java.util.Optional;

public interface PreferenciaRepository extends JpaRepository<PreferenciaComunicacion, Long> {

    Optional<PreferenciaComunicacion> findByPersonaId(Long idPersona);

    boolean existsByPersonaId(Long idPersona);
}