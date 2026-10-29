package upeu.edu.pe.Saludabemente.actividades.inscripcion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscrito;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscritoId;

import java.util.List;

public interface ActividadInscritoRepository
        extends JpaRepository<ActividadInscrito, ActividadInscritoId> {

    List<ActividadInscrito> findByIdActividad(Long idActividad);

    long countByIdActividad(Long idActividad);

    @Modifying
    @Query("DELETE FROM ActividadInscrito i WHERE i.idActividad = :idActividad")
    int deleteByIdActividad(@Param("idActividad") Long idActividad);
}