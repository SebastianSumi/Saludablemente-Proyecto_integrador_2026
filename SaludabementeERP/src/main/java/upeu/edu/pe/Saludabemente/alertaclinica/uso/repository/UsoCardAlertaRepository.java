package upeu.edu.pe.Saludabemente.alertaclinica.uso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.alertaclinica.uso.entity.UsoCardAlerta;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsoCardAlertaRepository extends JpaRepository<UsoCardAlerta, Long> {

    List<UsoCardAlerta> findByIdUsuario(Long idUsuario);

    Optional<UsoCardAlerta> findByIdUsuarioAndRuta(Long idUsuario, String ruta);

    void deleteByIdUsuario(Long idUsuario);
}
