package upeu.edu.pe.Saludabemente.asistencias.qr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.Saludabemente.asistencias.qr.entity.QrActividad;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrActividadRepository extends JpaRepository<QrActividad, Long> {

    Optional<QrActividad> findByCodigoQr(String codigoQr);

    Optional<QrActividad> findByActivoTrueAndIdActividad(Long idActividad);

    List<QrActividad> findByIdActividadOrderByVersionDesc(Long idActividad);

    Optional<QrActividad> findTopByIdActividadOrderByVersionDesc(Long idActividad);

    @Modifying
    @Query("UPDATE QrActividad q SET q.activo = false WHERE q.idActividad = :idActividad AND q.activo = true")
    int desactivarTodosByIdActividad(@Param("idActividad") Long idActividad);
}