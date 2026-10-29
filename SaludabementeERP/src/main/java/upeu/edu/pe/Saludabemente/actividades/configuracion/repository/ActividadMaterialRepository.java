package upeu.edu.pe.Saludabemente.actividades.configuracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.actividades.configuracion.entity.ActividadMaterial;

import java.util.List;

public interface ActividadMaterialRepository extends JpaRepository<ActividadMaterial, Long> {

    List<ActividadMaterial> findByIdActividad(Long idActividad);

    @Modifying
    @Query("DELETE FROM ActividadMaterial m WHERE m.idActividad = :idActividad")
    int deleteByIdActividad(@Param("idActividad") Long idActividad);
}