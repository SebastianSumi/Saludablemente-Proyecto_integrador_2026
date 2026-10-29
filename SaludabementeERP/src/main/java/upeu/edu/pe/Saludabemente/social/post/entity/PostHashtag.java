package upeu.edu.pe.Saludabemente.social.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="POST_HASHTAG",schema="SLB_SOCIAL",uniqueConstraints={@UniqueConstraint(columnNames={"ID_POST","TAG"})})
@Getter @Setter @NoArgsConstructor
public class PostHashtag {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST_HASHTAG") private Long idPostHashtag;
@Column(name="ID_POST",nullable=false) private Long idPost;
@Column(name="TAG",nullable=false,length=50) private String tag;
}