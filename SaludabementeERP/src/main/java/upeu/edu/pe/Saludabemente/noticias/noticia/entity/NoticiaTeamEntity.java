package upeu.edu.pe.Saludabemente.noticias.noticia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "NOTICIA_TEAM", schema = "SLB_NOTICIAS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(NoticiaTeamEntity.NoticiaTeamId.class)
public class NoticiaTeamEntity {

    @Id
    @Column(name = "ID_NOTICIA")
    private Long idNoticia;

    @Id
    @Column(name = "ID_TEAM")
    private Long idTeam;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticiaTeamId implements Serializable {
        private Long idNoticia;
        private Long idTeam;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NoticiaTeamId that)) return false;
            return Objects.equals(idNoticia, that.idNoticia)
                    && Objects.equals(idTeam, that.idTeam);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idNoticia, idTeam);
        }
    }
}