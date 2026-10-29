package upeu.edu.pe.Saludabemente.social.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import upeu.edu.pe.Saludabemente.social.api.dto.*;

@Entity
@Table(name="POST_IMAGEN",schema="SLB_SOCIAL")
@Getter @Setter @NoArgsConstructor
public class PostImagen {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_POST_IMAGEN") private Long idPostImagen;
@Column(name="ID_POST",nullable=false) private Long idPost;
@Column(name="URL",nullable=false,length=500) private String url;
@Column(name="ORDEN") private Integer orden;
}