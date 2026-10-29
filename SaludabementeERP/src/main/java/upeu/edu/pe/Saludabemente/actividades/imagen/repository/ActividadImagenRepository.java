package upeu.edu.pe.Saludabemente.actividades.imagen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.entity.ActividadImagen;

import java.util.List;

public interface ActividadImagenRepository extends JpaRepository<ActividadImagen, Long> {

    List<ActividadImagen> findByIdActividadOrderByOrdenAsc(Long idActividad);

    long countByIdActividad(Long idActividad);

    void deleteByIdActividad(Long idActividad);
}