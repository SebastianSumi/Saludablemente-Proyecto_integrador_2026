package upeu.edu.pe.Saludabemente.noticias.noticia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaTeamEntity;

import java.util.List;

public interface NoticiaTeamRepository extends JpaRepository<NoticiaTeamEntity, NoticiaTeamEntity.NoticiaTeamId> {

    List<NoticiaTeamEntity> findByIdNoticia(Long idNoticia);

    @Modifying
    @Query("DELETE FROM NoticiaTeamEntity nt WHERE nt.idNoticia = :idNoticia")
    void deleteByIdNoticia(@Param("idNoticia") Long idNoticia);

    @Query("SELECT nt.idNoticia FROM NoticiaTeamEntity nt WHERE nt.idTeam = :idTeam")
    List<Long> findIdsNoticiasByIdTeam(@Param("idTeam") Long idTeam);
}