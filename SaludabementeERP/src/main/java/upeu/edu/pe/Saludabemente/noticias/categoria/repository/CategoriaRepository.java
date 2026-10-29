package upeu.edu.pe.Saludabemente.noticias.categoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.noticias.categoria.entity.CategoriaEntity;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    Optional<CategoriaEntity> findByNombreAndActivoTrue(String nombre);

    List<CategoriaEntity> findByActivoTrueOrderByOrdenAsc();

    List<CategoriaEntity> findAllByOrderByOrdenAsc();
}