package upeu.edu.pe.Saludabemente.noticias.noticia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaFeedResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaEntity;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;

import java.util.List;
import java.util.Optional;

public interface NoticiaRepository extends JpaRepository<NoticiaEntity, Long> {

    long countByIdCategoria(Long idCategoria);

    Optional<NoticiaEntity> findByIdAndEstado(Long id, EstadoNoticia estado);

    @Query("""
        SELECT n.id AS id,
               n.titulo AS titulo,
               n.subtitulo AS subtitulo,
               c.nombre AS nombreCategoria,
               c.colorHex AS colorCategoria,
               n.imagenUrl AS imagenUrl,
               n.fechaPublicacion AS fechaPublicacion
        FROM NoticiaEntity n
        JOIN CategoriaEntity c ON c.id = n.idCategoria
        WHERE n.estado = upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO
        ORDER BY n.fechaPublicacion DESC
        """)
    List<NoticiaFeedProjection> findUltimasPublicadas(Pageable pageable);

    @Query("""
        SELECT n.id AS id,
               n.titulo AS titulo,
               n.subtitulo AS subtitulo,
               c.nombre AS nombreCategoria,
               c.colorHex AS colorCategoria,
               n.imagenUrl AS imagenUrl,
               n.fechaPublicacion AS fechaPublicacion
        FROM NoticiaEntity n
        JOIN CategoriaEntity c ON c.id = n.idCategoria
        WHERE n.id = :idNoticia
          AND n.estado = upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO
        """)
    Optional<NoticiaFeedProjection> findPublicadaPorId(@Param("idNoticia") Long idNoticia);

    @Query("""
        SELECT COUNT(n)
        FROM NoticiaEntity n
        WHERE n.idCategoria = :idCategoria
          AND n.estado = upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO
        """)
    long countPublicadasPorCategoria(@Param("idCategoria") Long idCategoria);

    @Query("""
        SELECT n FROM NoticiaEntity n
        WHERE (:estado IS NULL OR n.estado = :estado)
          AND (:idCategoria IS NULL OR n.idCategoria = :idCategoria)
          AND (:idArea IS NULL OR n.idArea = :idArea)
          AND (:idTeam IS NULL OR n.id IN :idsTeamFiltro)
        ORDER BY n.fechaCreacion DESC
        """)
    Page<NoticiaEntity> buscarAdmin(@Param("estado") EstadoNoticia estado,
                                    @Param("idCategoria") Long idCategoria,
                                    @Param("idArea") Long idArea,
                                    @Param("idTeam") Long idTeam,
                                    @Param("idsTeamFiltro") List<Long> idsTeamFiltro,
                                    Pageable pageable);

    @Query(value = """
        SELECT new upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaFeedResponseDTO(
            n.id, n.titulo, n.subtitulo, c.nombre, c.colorHex, n.imagenUrl, n.fechaPublicacion)
        FROM NoticiaEntity n
        JOIN CategoriaEntity c ON c.id = n.idCategoria
        WHERE n.estado = upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO
          AND (:idCategoria IS NULL OR n.idCategoria = :idCategoria)
          AND (n.idArea IS NULL OR :idArea IS NULL OR n.idArea = :idArea)
          AND (NOT EXISTS (SELECT 1 FROM NoticiaTeamEntity nt WHERE nt.idNoticia = n.id)
               OR EXISTS (SELECT 1 FROM NoticiaTeamEntity nt WHERE nt.idNoticia = n.id AND nt.idTeam = :idTeam))
        ORDER BY n.fechaPublicacion DESC
        """,
            countQuery = """
        SELECT COUNT(n)
        FROM NoticiaEntity n
        WHERE n.estado = upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia.PUBLICADO
          AND (:idCategoria IS NULL OR n.idCategoria = :idCategoria)
          AND (n.idArea IS NULL OR :idArea IS NULL OR n.idArea = :idArea)
          AND (NOT EXISTS (SELECT 1 FROM NoticiaTeamEntity nt WHERE nt.idNoticia = n.id)
               OR EXISTS (SELECT 1 FROM NoticiaTeamEntity nt WHERE nt.idNoticia = n.id AND nt.idTeam = :idTeam))
        """)
    Page<NoticiaFeedResponseDTO> buscarFeedPublico(@Param("idCategoria") Long idCategoria,
                                                   @Param("idArea") Long idArea,
                                                   @Param("idTeam") Long idTeam,
                                                   Pageable pageable);
}