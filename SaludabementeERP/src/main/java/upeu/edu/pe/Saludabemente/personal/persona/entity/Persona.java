package upeu.edu.pe.Saludabemente.personal.persona.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.catalogo.area.entity.AreaTrabajo;
import upeu.edu.pe.Saludabemente.teams.entity.Team;

import java.time.LocalDate;

@Entity
@Table(name = "PERSONA", schema = "SLB_PERSONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "celular", length = 15, unique = true)
    private String celular;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 1, nullable = false)
    private Sexo sexo;

    @Column(name = "email", length = 120)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_area")
    private AreaTrabajo areaTrabajo;

    @Column(name = "talla_polo", length = 5)
    private String tallaPolo;

    @Column(name = "estado_civil", length = 20)
    private String estadoCivil;

    @Column(name = "nivel_educativo", length = 30)
    private String nivelEducativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo")
    private Team equipo;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "documento_identidad", length = 15, unique = true, nullable = false)
    private String documentoIdentidad;

    @Column(name = "avatar_url", length = 250)
    private String avatarUrl;
}