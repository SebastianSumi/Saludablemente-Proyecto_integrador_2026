package upeu.edu.pe.Saludabemente.personal.preferencia.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;

import java.time.LocalTime;

@Entity
@Table(name = "PREFERENCIA_COMUNICACION", schema = "SLB_PERSONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaComunicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preferencia")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_persona", unique = true, nullable = false)
    private Persona persona;

    @Column(name = "canal_preferido", length = 30)
    private String canalPreferido;

    @Column(name = "horario_contacto_inicio")
    private LocalTime horarioContactoInicio;

    @Column(name = "horario_contacto_fin")
    private LocalTime horarioContactoFin;

    @Column(name = "acepta_recordatorios", nullable = false)
    @Builder.Default
    private Boolean aceptaRecordatorios = true;
}