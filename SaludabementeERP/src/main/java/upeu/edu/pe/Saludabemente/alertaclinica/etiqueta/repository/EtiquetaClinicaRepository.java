package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EtiquetaClinicaRepository extends JpaRepository<EtiquetaClinica, Long> {

    @Query("select distinct e from EtiquetaClinica e left join fetch e.parametros")
    List<EtiquetaClinica> findAllConParametros();

    @Query("""
        select new upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaResumenDto(
            e.id, e.codigo, e.nombre, e.colorHex, e.estado, count(distinct ep.id), count(distinct ae.id))
        from EtiquetaClinica e
        left join e.parametros ep
        left join AlertaEtiqueta ae on ae.idEtiqueta = e.id
        where (:estado is null or e.estado = :estado)
          and (:texto is null or lower(e.nombre) like lower(concat('%', :texto, '%'))
               or lower(e.codigo) like lower(concat('%', :texto, '%')))
          and (:idParametro is null or exists (
              select p.id from EtiquetaParametro p where p.etiqueta = e and p.idParametro = :idParametro))
        group by e.id, e.codigo, e.nombre, e.colorHex, e.estado
        order by e.codigo
        """)
    List<upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaResumenDto> buscarResumen(
            @Param("estado") String estado, @Param("texto") String texto,
            @Param("idParametro") Long idParametro);

    @Query(value = """
        select new upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaResumenDto(
            e.id, e.codigo, e.nombre, e.colorHex, e.estado, count(distinct ep.id), count(distinct ae.id))
        from EtiquetaClinica e
        left join e.parametros ep
        left join AlertaEtiqueta ae on ae.idEtiqueta = e.id
        where (:estado is null or e.estado = :estado)
          and (:texto is null or lower(e.nombre) like lower(concat('%', :texto, '%'))
               or lower(e.codigo) like lower(concat('%', :texto, '%')))
          and (:idParametro is null or exists (
              select p.id from EtiquetaParametro p where p.etiqueta = e and p.idParametro = :idParametro))
        group by e.id, e.codigo, e.nombre, e.colorHex, e.estado
        order by e.codigo
                """, countQuery = """
        select count(e) from EtiquetaClinica e
        where (:estado is null or e.estado = :estado)
          and (:texto is null or lower(e.nombre) like lower(concat('%', :texto, '%'))
               or lower(e.codigo) like lower(concat('%', :texto, '%')))
          and (:idParametro is null or exists (
              select p.id from EtiquetaParametro p where p.etiqueta = e and p.idParametro = :idParametro))
        """)
    org.springframework.data.domain.Page<upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.EtiquetaResumenDto> buscarResumenPaginado(
            @Param("estado") String estado, @Param("texto") String texto,
            @Param("idParametro") Long idParametro, org.springframework.data.domain.Pageable pageable);

    List<EtiquetaClinica> findByEstado(String estado);

    Optional<EtiquetaClinica> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /**
     * Query del motor: dado un conjunto de codigos de parametros
     * (que vienen de los tipoIndicador de los detalles), devuelve
     * todas las etiquetas ACTIVAS que tengan al menos uno de ellos.
     *
     * IMPORTANTE: el llamador debe validar que la coleccion no este vacia
     * antes de invocar este metodo (Oracle falla con IN () vacio).
     */
    @Query("""
        SELECT DISTINCT e FROM EtiquetaClinica e
        JOIN e.parametros ep
        WHERE e.estado = 'ACTIVO'
          AND ep.parametroCodigo IN :codigos
        """)
    List<EtiquetaClinica> findActivasPorCodigosParametros(
        @Param("codigos") Collection<String> codigos
    );
}
