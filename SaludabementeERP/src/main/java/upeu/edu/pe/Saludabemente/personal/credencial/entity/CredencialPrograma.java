package upeu.edu.pe.Saludabemente.personal.credencial.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;

import java.time.LocalDateTime;

@Entity
@Table(name = "CREDENCIAL_PROGRAMA", schema = "SLB_PERSONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredencialPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "codigo_qr_hash", length = 255, unique = true, nullable = false)
    private String codigoQrHash;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCredencial estado;
}